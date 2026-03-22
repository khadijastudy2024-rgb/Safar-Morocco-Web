from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from pydantic import BaseModel, ConfigDict
from typing import Optional, List
from database import engine, get_db, Base
import models
from service import generate_chat_response, client
import datetime

# Create tables
Base.metadata.create_all(bind=engine)

app = FastAPI(title="Safar Morocco Chatbot Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class ChatRequest(BaseModel):
    message: str
    sessionId: Optional[str] = None
    userId: Optional[int] = None

class MessageSchema(BaseModel):
    id: int
    role: str
    contenu: str
    timestamp: datetime.datetime

    model_config = ConfigDict(from_attributes=True)

@app.post("/api/chat")
async def chat(request: ChatRequest, db: Session = Depends(get_db)):
    print(f"\nReceived message: {request.message[:50]}...")
    
    response_text, conversation_id = generate_chat_response(
        db, request.message, request.sessionId, request.userId
    )
    return {"response": response_text, "conversation_id": conversation_id}

@app.get("/api/conversations")
async def get_conversations(userId: int, db: Session = Depends(get_db)):
    convs = db.query(models.Conversation).filter(models.Conversation.user_id == userId).order_by(models.Conversation.date_derniere_activite.desc()).all()
    return [{"id": c.session_id, "created_at": c.date_creation, "updated_at": c.date_derniere_activite} for c in convs]

@app.get("/api/conversations/{session_id}")
async def get_conversation_history(session_id: str, userId: int, db: Session = Depends(get_db)):
    conv = db.query(models.Conversation).filter(
        models.Conversation.session_id == session_id,
        models.Conversation.user_id == userId
    ).first()
    if not conv:
        raise HTTPException(status_code=403, detail="Access denied to this conversation")
    
    messages = db.query(models.Message).filter(models.Message.conversation_id == conv.id).order_by(models.Message.timestamp.asc()).all()
    return [{"id": m.id, "role": m.role, "contenu": m.contenu, "timestamp": m.timestamp} for m in messages]

@app.delete("/api/conversations/{session_id}")
async def delete_conversation(session_id: str, userId: int, db: Session = Depends(get_db)):
    conv = db.query(models.Conversation).filter(
        models.Conversation.session_id == session_id,
        models.Conversation.user_id == userId
    ).first()
    if not conv:
        raise HTTPException(status_code=403, detail="Access denied or conversation not found")
    
    db.delete(conv)
    db.commit()
    return {"success": True}

@app.get("/health")
async def health():
    return {
        "status": "ok",
        "api_configured": bool(client)
    }

@app.get("/")
async def root():
    return {
        "service": "Safar Morocco Chatbot",
        "status": "running",
        "endpoints": ["/api/chat", "/health"]
    }

if __name__ == "__main__":
    import uvicorn
    print("Starting Safar Morocco Chatbot Service on port 8000...")
    uvicorn.run(app, host="0.0.0.0", port=8000)
