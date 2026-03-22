import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

// Angular Material
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatCheckboxModule } from '@angular/material/checkbox';

// Components
import { HeaderComponent } from '../layout/header/header.component';
import { FooterComponent } from '../layout/footer/footer.component';
import { ChatbotComponent } from './components/chatbot.component';
import { DestinationCardComponent } from './components/destination-card/destination-card.component';
import { SkeletonLoaderComponent } from './components/skeleton-loader/skeleton-loader.component';
import { CountUpDirective } from './directives/count-up.directive';
import { DestinationDialogComponent } from '../destination/dialog/destination-dialog.component';
import { ImageUrlPipe } from './pipes/image-url.pipe';
import { CategoryNamePipe } from './pipes/category-name.pipe';
import { TranslateValuePipe } from './pipes/translate-value.pipe';
import { RecommendationComponent } from './components/recommendation/recommendation.component';

@NgModule({
    declarations: [
        HeaderComponent,
        FooterComponent,
        ChatbotComponent,
        DestinationCardComponent,
        SkeletonLoaderComponent,
        CountUpDirective,
        DestinationDialogComponent,
        ImageUrlPipe,
        CategoryNamePipe,
        TranslateValuePipe,
        RecommendationComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        MatButtonModule,
        MatToolbarModule,
        MatIconModule,
        MatCardModule,
        MatInputModule,
        MatFormFieldModule,
        MatMenuModule,
        MatProgressSpinnerModule,
        MatDialogModule,
        MatSnackBarModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatSelectModule,
        MatTabsModule,
        MatDividerModule,
        MatListModule,
        MatCheckboxModule,
        TranslateModule
    ],
    exports: [
        HeaderComponent,
        FooterComponent,
        ChatbotComponent,
        DestinationCardComponent,
        SkeletonLoaderComponent,
        CountUpDirective,
        DestinationDialogComponent,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        MatButtonModule,
        MatToolbarModule,
        MatIconModule,
        MatCardModule,
        MatInputModule,
        MatFormFieldModule,
        MatMenuModule,
        MatProgressSpinnerModule,
        MatDialogModule,
        MatSnackBarModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatSelectModule,
        MatTabsModule,
        MatDividerModule,
        MatListModule,
        MatCheckboxModule,
        TranslateModule,
        ImageUrlPipe,
        CategoryNamePipe,
        TranslateValuePipe,
        RecommendationComponent
    ]
})
export class SharedModule { }
