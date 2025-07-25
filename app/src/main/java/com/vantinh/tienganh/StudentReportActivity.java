package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class StudentReportActivity extends AppCompatActivity {

    private RecyclerView rvStudentReports;
    private TabLayout tabLayout;
    private TextView tvNoReports;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<StudentReport> studentReportList;
    private StudentReportAdapter studentReportAdapter;
    private String currentTab = "ALL"; // ALL, COMPLETED, IN_PROGRESS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_report);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        studentReportList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupTabs();
        setupRecyclerView();
        loadStudentReports();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        rvStudentReports = findViewById(R.id.rv_student_reports);
        tabLayout = findViewById(R.id.tab_layout);
        tvNoReports = findViewById(R.id.tv_no_reports);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Báo cáo kết quả học viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, TeacherDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_courses) {
                startActivity(new Intent(this, CourseManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_students) {
                startActivity(new Intent(this, EnrollmentManagementActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, UpdateProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Hoàn thành"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang học"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "ALL";
                        break;
                    case 1:
                        currentTab = "COMPLETED";
                        break;
                    case 2:
                        currentTab = "IN_PROGRESS";
                        break;
                }
                loadStudentReports();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        studentReportAdapter = new StudentReportAdapter(studentReportList, new StudentReportAdapter.OnReportClickListener() {
            @Override
            public void onViewDetail(StudentReport report) {
                Intent intent = new Intent(StudentReportActivity.this, StudentDetailReportActivity.class);
                intent.putExtra("studentId", report.getStudentId());
                intent.putExtra("studentName", report.getStudentName());
                intent.putExtra("courseId", report.getCourseId());
                intent.putExtra("courseName", report.getCourseName());
                startActivity(intent);
            }

            @Override
            public void onViewProgress(StudentReport report) {
                Intent intent = new Intent(StudentReportActivity.this, StudentProgressDetailActivity.class);
                intent.putExtra("studentId", report.getStudentId());
                intent.putExtra("courseId", report.getCourseId());
                startActivity(intent);
            }
        });
        rvStudentReports.setLayoutManager(new LinearLayoutManager(this));
        rvStudentReports.setAdapter(studentReportAdapter);
    }

    private void loadStudentReports() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String teacherId = mAuth.getCurrentUser().getUid();

        // Đơn giản hóa: Load tất cả enrollments và tạo report từ dữ liệu có sẵn
        db.collection("enrollments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentReportList.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Enrollment enrollment = Enrollment.fromMap(doc.getData());
                            enrollment.setId(doc.getId());

                            // Tạo StudentReport từ Enrollment
                            StudentReport report = createReportFromEnrollment(enrollment);
                            studentReportList.add(report);
                        }

                        updateUI();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private StudentReport createReportFromEnrollment(Enrollment enrollment) {
        StudentReport report = new StudentReport();

        // Sử dụng các method có sẵn trong class Enrollment mới
        report.setStudentId(enrollment.getStudentID()); // Sử dụng getStudentID() thay vì getStudentId()
        report.setStudentName(enrollment.getFullName()); // Sử dụng getFullName() thay vì getStudentName()
        report.setStudentEmail(enrollment.getStudentEmail());
        report.setCourseId(enrollment.getCourseID()); // Sử dụng getCourseID() thay vì getCourseId()
        report.setCourseName(enrollment.getCourseName());
        report.setEnrollmentDate(enrollment.getEnrollmentDate());

        // Set default values cho các trường khác
        report.setTotalQuizzes(0);
        report.setCompletedQuizzes(0);
        report.setAverageScore(0.0);
        report.setProgress(0.0);
        report.setStatus("NOT_STARTED");
        report.setLastActivity(enrollment.getEnrollmentDate());

        return report;
    }

    private boolean shouldIncludeInCurrentTab(StudentReport report) {
        switch (currentTab) {
            case "COMPLETED":
                return "COMPLETED".equals(report.getStatus());
            case "IN_PROGRESS":
                return "IN_PROGRESS".equals(report.getStatus()) || "NOT_STARTED".equals(report.getStatus());
            case "ALL":
            default:
                return true;
        }
    }

    private void updateUI() {
        // Filter reports theo tab hiện tại
        List<StudentReport> filteredReports = new ArrayList<>();
        for (StudentReport report : studentReportList) {
            if (shouldIncludeInCurrentTab(report)) {
                filteredReports.add(report);
            }
        }

        if (filteredReports.isEmpty()) {
            tvNoReports.setVisibility(View.VISIBLE);
            rvStudentReports.setVisibility(View.GONE);
        } else {
            tvNoReports.setVisibility(View.GONE);
            rvStudentReports.setVisibility(View.VISIBLE);
        }

        // Update adapter với filtered list
        studentReportAdapter = new StudentReportAdapter(filteredReports, new StudentReportAdapter.OnReportClickListener() {
            @Override
            public void onViewDetail(StudentReport report) {
                Intent intent = new Intent(StudentReportActivity.this, StudentDetailReportActivity.class);
                intent.putExtra("studentId", report.getStudentId());
                intent.putExtra("studentName", report.getStudentName());
                intent.putExtra("courseId", report.getCourseId());
                intent.putExtra("courseName", report.getCourseName());
                startActivity(intent);
            }

            @Override
            public void onViewProgress(StudentReport report) {
                Intent intent = new Intent(StudentReportActivity.this, StudentProgressDetailActivity.class);
                intent.putExtra("studentId", report.getStudentId());
                intent.putExtra("courseId", report.getCourseId());
                startActivity(intent);
            }
        });
        rvStudentReports.setAdapter(studentReportAdapter);
        studentReportAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
