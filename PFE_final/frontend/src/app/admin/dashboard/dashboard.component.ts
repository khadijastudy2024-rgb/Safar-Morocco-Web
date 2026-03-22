import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ChartConfiguration, ChartData, ChartOptions } from 'chart.js';
import { catchError } from 'rxjs/operators';
import { Subject, forkJoin, of } from 'rxjs';

@Component({
    standalone: false,
    selector: 'app-admin-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
    stats: any = {
        totalUsers: 0,
        activeUsers: 0,
        totalDestinations: 0,
        pendingReviews: 0,
        upcomingEvents: 0,
        userGrowth: {},
        categoryStats: {},
        activityStats: {}
    };
    logs: any[] = [];
    isLoading = true;
    currentDate = new Date();
    private destroy$ = new Subject<void>();

    // Chart: Destinations by Category (Doughnut)
    public doughnutChartOptions: ChartConfiguration['options'] = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'right',
                labels: { usePointStyle: true, font: { size: 12 } }
            }
        }
    };
    public doughnutChartData: ChartData<'doughnut'> = {
        labels: ['Cultural', 'Nature', 'Historical', 'Religious'],
        datasets: [{
            data: [15, 12, 10, 8],
            backgroundColor: ['#4facfe', '#43e97b', '#fa709a', '#fbc2eb'],
            borderWidth: 0,
            hoverOffset: 4
        }]
    };

    // Chart: User Growth (Line)
    public lineChartOptions: ChartOptions<'line'> = {
        responsive: true,
        maintainAspectRatio: false,
        elements: {
            line: { tension: 0.4 },
            point: { radius: 0, hitRadius: 10 }
        },
        scales: {
            y: {
                beginAtZero: true,
                grid: { display: false },
                ticks: { display: true }
            },
            x: {
                grid: { display: false }
            }
        },
        plugins: {
            legend: { display: false },
            tooltip: {
                backgroundColor: 'rgba(0,0,0,0.8)',
                padding: 12
            }
        }
    };
    public lineChartData: ChartData<'line'> = {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [{
            data: [65, 95, 120, 140, 165, 195],
            label: 'Travelers',
            fill: true,
            backgroundColor: 'rgba(79, 172, 254, 0.1)',
            borderColor: '#4facfe',
            borderWidth: 2
        }]
    };

    // Chart: Activity Volume (Bar)
    public barChartOptions: ChartOptions<'bar'> = {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            y: { grid: { display: false }, beginAtZero: true },
            x: { grid: { display: false } }
        },
        plugins: { legend: { display: false } }
    };
    public barChartData: ChartData<'bar'> = {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [{
            data: [42, 58, 75, 68, 82, 95],
            label: 'Activity',
            backgroundColor: '#43e97b',
            borderRadius: 4
        }]
    };

    adminUser: any;

    constructor(
        private apiService: ApiService,
        private authService: AuthService,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.adminUser = this.authService.currentUserValue;
        this.loadDashboardData();
    }

    loadDashboardData() {
        this.isLoading = true;

        forkJoin({
            stats: this.apiService.getAdminStats().pipe(
                catchError((err: any) => {
                    console.error('Failed to load stats', err);
                    return of(this.stats);
                })
            ),
            logs: this.apiService.getActivityLogs().pipe(
                catchError((err: any) => {
                    console.error('Failed to load logs', err);
                    return of([]);
                })
            )
        }).subscribe({
            next: ({ stats, logs }) => {
                this.stats = stats || this.stats;
                this.logs = logs ? logs.slice(0, 5) : [];
                this.updateCharts();
                this.isLoading = false;
                this.cd.detectChanges();
            },
            error: (err: any) => {
                console.error('Critical dashboard error:', err);
                this.isLoading = false;
                this.cd.detectChanges();
            }
        });
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    updateCharts() {
        if (!this.stats) return;

        // Update Line Chart (User Growth)
        if (this.stats.userGrowth && Object.keys(this.stats.userGrowth).length > 0) {
            this.lineChartData = {
                labels: Object.keys(this.stats.userGrowth),
                datasets: [{
                    data: Object.values(this.stats.userGrowth) as number[],
                    label: 'New Travelers',
                    fill: true,
                    tension: 0.4,
                    borderColor: '#4facfe',
                    backgroundColor: 'rgba(79, 172, 254, 0.1)',
                    pointRadius: 0
                }]
            };
        }

        // Update Doughnut Chart (Categories)
        if (this.stats.categoryStats && Object.keys(this.stats.categoryStats).length > 0) {
            const categories = Object.entries(this.stats.categoryStats)
                .sort((a: any, b: any) => b[1] - a[1]) // Sort largest first
                .slice(0, 5); // Top 5

            this.doughnutChartData = {
                labels: categories.map(c => c[0]),
                datasets: [{
                    data: categories.map(c => c[1] as number),
                    backgroundColor: ['#4facfe', '#43e97b', '#fa709a', '#fbc2eb', '#ffb86c'],
                    borderWidth: 0,
                    hoverOffset: 4
                }]
            };
        }

        // Update Bar Chart (Recent Events Duration)
        if (this.stats.eventStats && Object.keys(this.stats.eventStats).length > 0) {
            this.barChartData = {
                labels: Object.keys(this.stats.eventStats),
                datasets: [{
                    data: Object.values(this.stats.eventStats) as number[],
                    label: 'Duration (Days)',
                    backgroundColor: '#43e97b',
                    borderRadius: 4,
                    barThickness: 20
                }]
            };
        }
    }
}
