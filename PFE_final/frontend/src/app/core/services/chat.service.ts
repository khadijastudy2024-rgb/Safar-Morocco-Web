import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8000/api';

  constructor(private http: HttpClient) { }

  sendMessage(message: string, sessionId: string | null, userId: number | null): Observable<{ response: string, conversation_id: string }> {
    return this.http.post<{ response: string, conversation_id: string }>(`${this.apiUrl}/chat`, { message, sessionId, userId }).pipe(
      catchError(error => {
        console.error('Error sending message to chatbot API', error);
        return throwError(() => new Error('Failed to get response from chatbot'));
      })
    );
  }

  getConversations(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/conversations?userId=${userId}`).pipe(
      catchError(error => {
        console.error('Error fetching conversations', error);
        return throwError(() => new Error('Failed to fetch conversations'));
      })
    );
  }

  getConversationHistory(conversationId: string, userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/conversations/${conversationId}?userId=${userId}`).pipe(
      catchError(error => {
        console.error('Error fetching conversation history', error);
        return throwError(() => new Error('Failed to fetch conversation history'));
      })
    );
  }
  clearConversation(sessionId: string, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/conversations/${sessionId}?userId=${userId}`).pipe(
      catchError(error => {
        console.error('Error clearing conversation', error);
        return throwError(() => new Error('Failed to clear conversation'));
      })
    );
  }
}
