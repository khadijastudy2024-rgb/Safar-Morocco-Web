import os
import re
import warnings
warnings.filterwarnings("ignore", category=FutureWarning, module="google.generativeai")
import google.generativeai as genai
from google.api_core import exceptions
from dotenv import load_dotenv
import requests
import models
from sqlalchemy.orm import Session
import uuid
from typing import Optional, List, Tuple

# Load environment variables
load_dotenv()

# ─── Configure Gemini API ──────────────────────────────────────────────────────
# Accepts either GOOGLE_API_KEY or GENAI_API_KEY for backward compatibility
GENAI_API_KEY = os.getenv("GENAI_API_KEY") or os.getenv("GOOGLE_API_KEY")
client = None  # kept as alias so main.py health check still works
model = None

# Models to try in order — updated to use available Gemini 2.x models
MODELS_TO_TRY = [
    "gemini-2.0-flash",
    "gemini-2.0-flash-lite",
    "gemini-2.5-flash",
    "gemini-2.5-pro",
]

if GENAI_API_KEY:
    try:
        genai.configure(api_key=GENAI_API_KEY)
        model = genai.GenerativeModel(MODELS_TO_TRY[0])
        client = model  # alias for health check in main.py
        print(f"✅ Gemini SDK configured — using Gemini 2.x models")
        print("📋 Available models on your API key:")
        try:
            for m in genai.list_models():
                if "generateContent" in (m.supported_generation_methods or []):
                    print(f"   ✓ {m.name}")
        except Exception as le:
            print(f"   (Could not list models: {le})")
    except Exception as e:
        print(f"❌ Error initializing Gemini: {e}")
else:
    print("⚠️  WARNING: No API key found. Set GOOGLE_API_KEY or GENAI_API_KEY in chatbot/.env")


def list_available_models():
    """Debug helper — prints all models available to the configured API key."""
    try:
        for m in genai.list_models():
            print(m.name)
    except Exception as e:
        print(f"Could not list models: {e}")


# ─── Config ───────────────────────────────────────────────────────────────────
BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8088/api")

SYSTEM_PROMPT = """
You are 'Safar Assistant', a helpful and passionate AI travel expert specializing in Morocco.
Your goal is to help users discover the beauty, culture, and history of the Kingdom of Morocco.

CRITICAL INSTRUCTIONS:
1. RESPONSE LENGTH: Keep answers SHORT and CONCISE (2-3 sentences max for simple questions).
2. LANGUAGE MATCHING: Always respond in the EXACT SAME LANGUAGE as the user's message.
3. TONE: Be warm, professional, and enthusiastic about Morocco.
4. LOCAL TOUCH: Use occasional Moroccan Darija words like 'Marhaba' (welcome) where appropriate.
"""


def fetch_destinations():
    try:
        response = requests.get(f"{BACKEND_URL}/destinations", timeout=5)
        if response.status_code == 200:
            return response.json()
    except Exception as e:
        print(f"Error fetching destinations: {e}")
    return []


from typing import Optional, List, Tuple


def save_message_to_db(db: Session, conversation_id: int, role: str, message: str, user_id: Optional[int] = None):
    if not conversation_id:
        return
    try:
        new_message = models.Message(
            conversation_id=conversation_id,
            user_id=user_id,
            role=role,
            contenu=message
        )
        db.add(new_message)
        db.commit()
    except Exception as e:
        print(f"Error saving message to database: {e}")


def save_fact(db: Session, user_id: int, key: str, value: str):
    existing = db.query(models.UserMemory).filter(
        models.UserMemory.user_id == user_id,
        models.UserMemory.memory_key == key
    ).first()

    if existing:
        print(f"DEBUG: Updating existing memory '{key}' for user {user_id}")
        existing.memory_value = value
    else:
        print(f"DEBUG: Creating new memory '{key}' for user {user_id}")
        new_memory = models.UserMemory(
            user_id=user_id,
            memory_key=key,
            memory_value=value
        )
        db.add(new_memory)
    db.commit()


def detect_user_facts(db: Session, user_id: Optional[int], message: str):
    print(f"DEBUG: detect_user_facts called for user_id={user_id}")
    if not user_id:
        return

    # 1. NAME DETECTION
    name_patterns = [
        r"(?:my name is|i am|i'm|call me|this is)\s+([a-z\u00C0-\u017F\s\-]+)",
        r"(?:je m'appelle|mon nom est|je suis|appelez-moi)\s+([a-z\u00C0-\u017F\s\-]+)",
        r"(?:soy|me llamo|mi nombre es)\s+([a-z\u00C0-\u017F\s\-]+)",
        r"(?:ismi|ismy|ana|ana ismi)\s+([a-z\u00C0-\u017F\s\-]+)"
    ]
    for pattern in name_patterns:
        match = re.search(pattern, message, re.IGNORECASE)
        if match:
            name = match.group(1).strip().rstrip('.!? ')
            if 2 <= len(name) <= 50:
                save_fact(db, user_id, "name", name)
                break

    # 2. INTEREST/PREFERENCE DETECTION
    interest_patterns = [
        r"(?:i like|i love|j'aime|j'adore|me gusta|i'm interested in)\s+([a-z\u00C0-\u017F\s\-]+)",
        r"(?:i prefer|je préfère|prefiero)\s+([a-z\u00C0-\u017F\s\-]+)"
    ]
    for pattern in interest_patterns:
        match = re.search(pattern, message, re.IGNORECASE)
        if match:
            interest = match.group(1).strip().rstrip('.!? ')
            if 2 <= len(interest) <= 100:
                save_fact(db, user_id, "interest", interest)
                break

    # 3. ORIGIN DETECTION
    origin_patterns = [
        r"(?:i'm from|i come from|je viens de|je suis de|soy de)\s+([a-z\u00C0-\u017F\s\-]+)"
    ]
    for pattern in origin_patterns:
        match = re.search(pattern, message, re.IGNORECASE)
        if match:
            origin = match.group(1).strip().rstrip('.!? ')
            if 2 <= len(origin) <= 50:
                save_fact(db, user_id, "origin", origin)
                break


def get_fallback_response(message: str, last_error_type: Optional[str]) -> str:
    print("Attempting smart fallback with local data...")
    if last_error_type in ["QUOTA", "UNKNOWN", "TEXT_ACCESS_ERROR"]:
        try:
            dests = fetch_destinations()
            lower_msg = message.lower()

            greetings = ['hello', 'hi', 'salam', 'marhaba', 'hey', 'bonjour']
            if any(g in lower_msg for g in greetings) and len(lower_msg) < 20:
                return "Marhaba! I'm currently in 'Offline Mode' due to high traffic, but I can still share information about our destinations. Try asking about 'Agadir', 'Marrakech', or other cities!"

            found = []
            for d in dests:
                name = (d.get('name') or '').lower()
                city = (d.get('city') or '').lower()
                if (name in lower_msg and len(name) > 3) or (city in lower_msg and len(city) > 3):
                    found.append(d)

            if found:
                target = found[0]
                desc = target.get('description', '')
                if len(desc) > 300:
                    desc = desc[:300] + "..."
                return f"Since I'm in offline mode, I retrieved this from my local database:\n\n**{target.get('name') or 'Unknown'}** ({target.get('city') or 'Unknown'})\n\n{desc}\n\n*This is an automated fallback response.*"

            city_names = sorted(list(set(d.get('city') or 'Unknown' for d in dests)))
            # Avoid slicing/indexing for strict IDE compatibility
            short_city_list = list(city_names)
            while len(short_city_list) > 10:
                short_city_list.pop()
            cities_str = ", ".join(short_city_list)

            return f"I'm currently offline due to high traffic.\n\nI couldn't find a match for your query in my local records, but I have information about these cities:\n**{cities_str}**.\n\nTry asking about one of them (e.g., 'Marrakech')!"

        except Exception as e:
            print(f"Fallback logic failed: {e}")

    if last_error_type == "QUOTA":
        return "I'm currently experiencing very high demand and my AI brain is resting.\n\nPlease try asking about a specific city (like 'Casablanca') - I might be able to find it in my offline records!"
    elif last_error_type == "NOT_FOUND":
        return "The AI service is temporarily unavailable. Please try again later."
    elif last_error_type in ["EMPTY_RESPONSE", "EMPTY_TEXT", "NO_TEXT_ATTR", "TEXT_ACCESS_ERROR"]:
        return "I received your message but couldn't generate a proper response."
    elif last_error_type == "PERMISSION_DENIED":
        return "The AI service is currently unavailable due to a configuration issue. Please try again later."
    else:
        return "System Error. Please try again later."


def generate_chat_response(db: Session, message: str, session_id: Optional[str] = None, user_id: Optional[int] = None) -> Tuple[str, Optional[str]]:
    if not model:
        return "Sorry, the chatbot service is not configured properly. Please check the API key.", session_id

    # Handle Conversation
    conv = None
    if session_id:
        query = db.query(models.Conversation).filter(models.Conversation.session_id == session_id)
        if user_id:
            query = query.filter(models.Conversation.user_id == user_id)
        conv = query.first()

        # Security: If a session_id was provided but not found for this user,
        # do NOT use it. Reset to a new conversation.
        if not conv and session_id:
            print(f"SECURITY: Session {session_id} not found for user {user_id}. Creating new session.")
            session_id = None

    if not conv:
        conv = models.Conversation(session_id=session_id or str(uuid.uuid4()), user_id=user_id)
        db.add(conv)
        db.commit()
        db.refresh(conv)
        session_id = conv.session_id

    conversation_internal_id = conv.id

    save_message_to_db(db, conversation_internal_id, "user", message, user_id)

    # Detect and save facts
    detect_user_facts(db, user_id, message)

    # Load history for this user (cross-session if requested, or just this session)
    if user_id:
        history_records = db.query(models.Message).filter(
            models.Message.user_id == user_id
        ).order_by(models.Message.timestamp.asc()).all()
    else:
        history_records = db.query(models.Message).filter(
            models.Message.conversation_id == conversation_internal_id
        ).order_by(models.Message.timestamp.asc()).all()

    # Load User Memories
    user_memories = []
    if user_id:
        user_memories = db.query(models.UserMemory).filter(models.UserMemory.user_id == user_id).all()

    last_error_type = None

    for model_name in MODELS_TO_TRY:
        try:
            print(f"Trying model: {model_name}")
            current_model = genai.GenerativeModel(model_name)

            # Build system context
            context = SYSTEM_PROMPT
            if user_memories:
                context += "\n\nInformation about the user:\n"
                for m in user_memories:
                    context += f"- {m.memory_key}: {m.memory_value}\n"

            # Build structured conversation for the API
            contents = []
            contents.append({"role": "user", "parts": [{"text": context + "\n\nPlease acknowledge your role."}]})
            contents.append({"role": "model", "parts": [{"text": "I am Safar Assistant. I am ready to help you."}]})

            # Add recent history (exclude the message just saved)
            history = list(history_records or [])
            if len(history) > 0:
                history.pop()
            while len(history) > 10:
                history.pop(0)

            for msg in history:
                role = "user" if msg.role == "user" else "model"
                contents.append({"role": role, "parts": [{"text": msg.contenu}]})

            contents.append({"role": "user", "parts": [{"text": message}]})

            print(f"Sending request to {model_name}...")
            response = current_model.generate_content(contents)

            if not response:
                last_error_type = "EMPTY_RESPONSE"
                continue

            try:
                bot_response = response.text
                if not bot_response or bot_response.strip() == "":
                    last_error_type = "EMPTY_TEXT"
                    continue
            except AttributeError:
                last_error_type = "NO_TEXT_ATTR"
                continue
            except Exception:
                last_error_type = "TEXT_ACCESS_ERROR"
                continue

            save_message_to_db(db, conversation_internal_id, "assistant", bot_response, user_id)
            return bot_response, session_id

        except exceptions.PermissionDenied as e:
            print(f"[ERROR] {model_name} → 403 PERMISSION_DENIED: {e}")
            last_error_type = "PERMISSION_DENIED"
            break  # Wrong key — no point trying other models
        except exceptions.ResourceExhausted as e:
            print(f"[ERROR] {model_name} → 429 QUOTA EXHAUSTED. Get a new key at aistudio.google.com")
            last_error_type = "QUOTA"
            continue
        except exceptions.NotFound as e:
            print(f"[ERROR] {model_name} → 404 NOT_FOUND (trying next model)")
            if last_error_type != "QUOTA":
                last_error_type = "NOT_FOUND"
            continue
        except exceptions.InvalidArgument as e:
            print(f"[ERROR] {model_name} → 400 INVALID_ARGUMENT: {e}")
            if last_error_type != "QUOTA":
                last_error_type = "INVALID_ARG"
            continue
        except Exception as e:
            err_msg = str(e)
            print(f"[ERROR] {model_name} → {err_msg}")
            if "429" in err_msg or "QUOTA" in err_msg.upper():
                last_error_type = "QUOTA"
            elif "404" in err_msg or "NOT_FOUND" in err_msg.upper():
                if last_error_type != "QUOTA":
                    last_error_type = "NOT_FOUND"
            elif "403" in err_msg or "PERMISSION_DENIED" in err_msg.upper():
                last_error_type = "PERMISSION_DENIED"
                break
            else:
                last_error_type = last_error_type or "UNKNOWN"
            continue

    print(f"Request failed. Error type: {last_error_type}")
    fallback = get_fallback_response(message, last_error_type)
    save_message_to_db(db, conversation_internal_id, "assistant", fallback, user_id)
    return fallback, session_id
