import { Component, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef } from '@angular/core';
import { ChatService } from '../../core/services/chat.service';
import { TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-chatbot',
  template: `
    <div class="chatbot-trigger" [class.hidden]="isOpen" (click)="toggle()" *ngIf="authService.isLoggedIn">
        <i class="bi bi-chat-dots-fill" style="font-size: 1.5rem;"></i>
    </div>
    <div class="chatbot-container" [class.open]="isOpen" [ngStyle]="{'width': showSidebar ? '600px' : '380px'}" *ngIf="authService.isLoggedIn">
      <div class="chatbot-header">
        <span (click)="toggle()">{{ 'CHATBOT.HEADER' | translate }}</span>
        <div class="header-actions d-flex gap-3 align-items-center">
          <i class="bi bi-clock-history" *ngIf="currentUser" (click)="toggleSidebar()" title="Chat History" style="cursor:pointer;"></i>
          <i class="bi bi-x-lg" (click)="toggle()" style="cursor:pointer;"></i>
        </div>
      </div>
      <div class="chatbot-body" *ngIf="isOpen">
        <div class="chatbot-sidebar" *ngIf="showSidebar">
          <button class="new-chat-btn" (click)="startNewChat()">
            <i class="bi bi-plus-lg me-2"></i> New Chat
          </button>
          
          <div class="history-list mt-3">
             <div *ngIf="conversations.length === 0" class="text-muted small text-center mt-4">
                 No previous conversations
             </div>
             <div class="history-item" *ngFor="let conv of conversations" 
                  [class.active]="conv.id === sessionId"
                  (click)="loadConversation(conv.id)">
               <div class="text-truncate" style="max-width: 150px;">
                 <i class="bi bi-chat-left-text me-2"></i> {{ conv.created_at | date:'mediumDate' }}
               </div>
               <small class="text-muted d-block" style="font-size: 0.75rem;">{{ conv.created_at | date:'shortTime' }}</small>
             </div>
          </div>
        </div>
        
        <div class="chat-main" [ngStyle]="{'width': showSidebar ? '400px' : '100%'}">
          <div class="messages" #scrollContainer>
            <div *ngFor="let msg of messages" [class.user]="msg.sender === 'user'" [class.bot]="msg.sender === 'bot'" class="message">
              {{ msg.text }}
            </div>
            <div *ngIf="isLoading" class="message bot typing-indicator">
              <span>.</span><span>.</span><span>.</span>
            </div>
          </div>
          <div class="input-area">
            <input [(ngModel)]="userInput" (keyup.enter)="sendMessage()" [placeholder]="'CHATBOT.PLACEHOLDER' | translate">
            <button class="send-btn" (click)="sendMessage()">
              <i class="bi bi-send-fill"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

  `,
  styles: [`
    .chatbot-container {
      position: fixed;
      bottom: 30px;
      right: 30px;
      width: 380px;
      z-index: 2000;
      background: white;
      box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25);
      border-radius: 24px;
      overflow: hidden;
      transform: translateY(120%);
      transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      border: 1px solid rgba(0,0,0,0.05);
    }
    .chatbot-container.open {
        transform: translateY(0);
    }
    .chatbot-header {
      background: linear-gradient(135deg, #0F766E, #F59E0B);
      color: white;
      padding: 18px 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    .chatbot-header span {
      font-weight: 800; 
      font-size: 1.15rem; 
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
    }
    .chatbot-header span::before {
      content: '✨';
      font-size: 1.2rem;
    }
    
    .chatbot-trigger {
        position: fixed;
        bottom: 30px;
        right: 30px;
        width: 64px; 
        height: 64px;
        background: linear-gradient(135deg, #F59E0B, #FBBF24);
        color: white;
        border-radius: 50%;
        display: flex; 
        align-items: center; 
        justify-content: center;
        box-shadow: 0 8px 24px rgba(245, 158, 11, 0.4);
        cursor: pointer;
        z-index: 1500;
        transition: all 0.3s ease;
    }
    .chatbot-trigger:hover { 
      transform: scale(1.1); 
      box-shadow: 0 12px 32px rgba(245, 158, 11, 0.5);
    }
    .chatbot-trigger.hidden { display: none; }
    
    .chatbot-body {
      height: 450px;
      display: flex;
      flex-direction: row;
      background: #F9FAFB;
    }
    .chatbot-sidebar {
      width: 200px;
      background: white;
      border-right: 1px solid #E5E7EB;
      display: flex;
      flex-direction: column;
      padding: 16px;
    }
    .new-chat-btn {
      width: 100%;
      background: #F1F5F9;
      color: #0F766E;
      border: 1px dashed #94A3B8;
      padding: 10px;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }
    .new-chat-btn:hover {
      background: #E2E8F0;
      border-color: #0F766E;
    }
    .history-list {
      flex: 1;
      overflow-y: auto;
    }
    .history-item {
      padding: 10px;
      border-radius: 8px;
      cursor: pointer;
      margin-bottom: 8px;
      transition: background 0.2s;
    }
    .history-item:hover {
      background: #F8FAFC;
    }
    .history-item.active {
      background: #F0FDF4;
      border-left: 3px solid #10B981;
    }
    .chat-main {
      flex: 1;
      display: flex;
      flex-direction: column;
    }
    .messages {
      flex: 1;
      padding: 24px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .message {
      padding: 12px 18px;
      border-radius: 20px;
      max-width: 85%;
      font-size: 0.95rem;
      line-height: 1.5;
      animation: fadeIn 0.3s ease-out;
      box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    }
    .user {
      background: linear-gradient(135deg, #F59E0B, #FBBF24);
      color: white;
      align-self: flex-end;
      border-bottom-right-radius: 6px;
      font-weight: 500;
    }
    .bot {
      background: white;
      color: #111827;
      align-self: flex-start;
      border-bottom-left-radius: 6px;
      border: 1px solid #E5E7EB;
    }
    .input-area {
      display: flex;
      background: white;
      padding: 16px;
      border-top: 1px solid #E5E7EB;
      align-items: center;
      gap: 12px;
    }
    input {
      flex: 1;
      border: 2px solid #E5E7EB;
      padding: 12px 20px;
      border-radius: 28px;
      outline: none;
      background: #F9FAFB;
      font-size: 0.95rem;
      transition: all 0.2s;
      font-weight: 500;
    }
    input:focus { 
      background: white; 
      border-color: #F59E0B; 
      box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.1); 
    }
    
    .send-btn {
      background: linear-gradient(135deg, #F59E0B, #FBBF24);
      color: white;
      border: none;
      width: 44px;
      height: 44px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
      flex-shrink: 0;
      box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
    }
    .send-btn:hover { 
      transform: scale(1.08); 
      box-shadow: 0 6px 16px rgba(245, 158, 11, 0.4);
    }
    .send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

    
    @keyframes fadeIn { 
      from { opacity: 0; transform: translateY(8px); } 
      to { opacity: 1; transform: translateY(0); } 
    }
    
    .typing-indicator span {
      animation: blink 1.4s infinite both;
      font-size: 1.5rem;
      line-height: 10px;
      margin: 0 1px;
    }
    .typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
    .typing-indicator span:nth-child(3) { animation-delay: 0.4s; }
    
    @keyframes blink {
      0% { opacity: 0.2; }
      20% { opacity: 1; }
      100% { opacity: 0.2; }
    }
  `]
})
export class ChatbotComponent implements AfterViewChecked {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;
  isOpen = false;
  userInput = '';
  isLoading = false;
  messages: { text: string, sender: string }[] = [];

  sessionId: string | null = null;
  showSidebar = false;
  conversations: any[] = [];
  currentUser: any = null;

  constructor(
    private chatService: ChatService,
    private cd: ChangeDetectorRef,
    private translate: TranslateService,
    public authService: AuthService,
    private router: Router
  ) {
    this.authService.user$.subscribe((user: any) => {
      // If user changed or logged out, reset chat
      if (this.currentUser && (!user || user.id !== this.currentUser.id)) {
        this.sessionId = null;
        localStorage.removeItem('chat_session_id');
        this.messages = [];
        this.addInitialMessage();
        this.showSidebar = false;
        this.conversations = [];
      }
      this.currentUser = user;
    });

    this.sessionId = localStorage.getItem('chat_session_id');

    if (!this.sessionId) {
      this.addInitialMessage();
    } else {
      // If we have a sessionId, we still show the welcome message until history loads (or load it immediately if open)
      this.addInitialMessage();
    }
  }

  addInitialMessage() {
    const lang = this.translate.currentLang || 'en';
    let welcomeText = "Hello 👋 I am Safar Assistant. How can I help you explore Morocco today?";

    if (lang === 'fr') {
      welcomeText = "Bonjour 👋 Je suis l'assistant Safar. Comment puis-je vous aider ?";
    } else if (lang === 'en') {
      welcomeText = "Hello 👋 I am Safar Assistant. How can I help you?";
    }

    this.messages = [{ text: welcomeText, sender: 'bot' }];
  }

  // generateSessionId removed as it's handled by DB now

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch (err) { }
  }

  toggle() {
    if (!this.authService.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      if (this.sessionId && this.messages.length === 0) {
        this.loadConversation(this.sessionId);
      }
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  toggleSidebar() {
    this.showSidebar = !this.showSidebar;
    if (this.showSidebar && this.currentUser) {
      this.loadConversationsList();
    }
  }

  loadConversationsList() {
    if (!this.currentUser) return;
    this.chatService.getConversations(this.currentUser.id).subscribe({
      next: (convs) => this.conversations = convs,
      error: (err) => console.error("Could not load conversations", err)
    });
  }

  loadConversation(id: string) {
    this.sessionId = id;
    localStorage.setItem('chat_session_id', id);
    this.messages = [];
    this.isLoading = true;
    if (!this.currentUser) {
      this.isLoading = false;
      return;
    }
    this.chatService.getConversationHistory(id, this.currentUser.id).subscribe({
      next: (msgs) => {
        this.messages = msgs.map(m => ({
          text: m.contenu,
          sender: m.role === 'user' ? 'user' : 'bot'
        }));
        if (this.messages.length === 0) {
          this.addInitialMessage();
        }
        this.isLoading = false;
        this.cd.detectChanges();
        this.scrollToBottom();

        // Remove active state sidebar if mobile sized? Optional
      },
      error: (err) => {
        this.isLoading = false;
        this.addInitialMessage();
      }
    });
  }

  startNewChat() {
    this.sessionId = null;
    localStorage.removeItem('chat_session_id');
    this.messages = [];
    this.addInitialMessage();
    if (window.innerWidth < 600) {
      this.showSidebar = false;
    }
  }

  sendMessage() {
    if (!this.userInput.trim()) return;

    this.messages.push({ text: this.userInput, sender: 'user' });
    const text = this.userInput;
    this.userInput = '';
    this.isLoading = true;

    const userId = this.authService.currentUserValue?.id || null;

    this.chatService.sendMessage(text, this.sessionId, userId).subscribe({
      next: (res) => {
        this.messages.push({ text: res.response, sender: 'bot' });
        if (!this.sessionId && res.conversation_id) {
          this.sessionId = res.conversation_id;
          localStorage.setItem('chat_session_id', this.sessionId);
          if (this.showSidebar) this.loadConversationsList();
        }
        this.isLoading = false;
        this.cd.detectChanges();
        this.scrollToBottom();
      },
      error: () => {
        const errorMsg = this.translate.instant('CHATBOT.ERROR');
        this.messages.push({ text: errorMsg, sender: 'bot' });
        this.isLoading = false;
        this.cd.detectChanges();
        this.scrollToBottom();
      }
    });
  }
}
