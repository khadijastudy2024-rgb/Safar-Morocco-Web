import uuid
import datetime
from sqlalchemy import Column, String, Integer, DateTime, ForeignKey, Text, Boolean
from sqlalchemy.orm import relationship
from database import Base

class Conversation(Base):
    __tablename__ = "conversations"

    id = Column(Integer, primary_key=True, autoincrement=True)
    session_id = Column(String(255), unique=True, nullable=False)
    user_id = Column(Integer, index=True, nullable=True)
    langue = Column(String(5), default="fr")
    date_creation = Column(DateTime, default=datetime.datetime.utcnow)
    date_derniere_activite = Column(DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)
    active = Column(Boolean, default=True)

    messages = relationship("Message", back_populates="conversation", cascade="all, delete-orphan", passive_deletes=True)

class Message(Base):
    __tablename__ = "chat_messages"

    id = Column(Integer, primary_key=True, autoincrement=True)
    conversation_id = Column(Integer, ForeignKey("conversations.id", ondelete="CASCADE"), index=True)
    user_id = Column(Integer, index=True, nullable=True) # Direct link for easier querying by user
    role = Column(String(20), nullable=False)  # 'user' | 'assistant'
    contenu = Column(Text, nullable=False)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)

    conversation = relationship("Conversation", back_populates="messages")

class UserMemory(Base):
    __tablename__ = "user_memory"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, index=True, nullable=False)
    memory_key = Column(String(100), nullable=False)
    memory_value = Column(Text, nullable=False)
