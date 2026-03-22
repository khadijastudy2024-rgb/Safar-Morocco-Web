import re

def test_detect_user_facts(message):
    patterns = [
        r"(?:my name is|i am|i'm|call me|this is)\s+([a-z\u00C0-\u017F\s\-]+)",
        r"(?:je m'appelle|mon nom est|je suis|appelez-moi)\s+([a-z\u00C0-\u017F\s\-]+)",
        r"(?:soy|me llamo|mi nombre es)\s+([a-z\u00C0-\u017F\s\-]+)",
        r"(?:ismi|ismy|ana|ana ismi)\s+([a-z\u00C0-\u017F\s\-]+)"
    ]
    
    detected_name: str | None = None
    for pattern in patterns:
        match = re.search(pattern, message, re.IGNORECASE)
        if match:
            temp_name = match.group(1).strip()
            if 2 <= len(temp_name) <= 50:
                detected_name = temp_name
                break
    
    if isinstance(detected_name, str):
        detected_name = detected_name.rstrip('.!? ')
    return detected_name

# Test cases
test_cases = [
    "My name is Ahmed",
    "Je m'appelle Thomas",
    "I am Sarah",
    "I'm John",
    "Call me Yassine",
    "Mon nom est Pierre.",
    "Je suis Fatima!",
    "Ismi Omar",
    "Ana ismi Khalid",
    "Soy Maria",
    "Me llamo Juan",
    "This is Robert",
    "Random message without name",
    "I am a very long sentence that should probably not be a name but let's see"
]

for tc in test_cases:
    print(f"Input: '{tc}' -> Detected: '{test_detect_user_facts(tc)}'")
