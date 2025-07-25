package com.vantinh.tienganh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vantinh.tienganh.utils.RealtimeManager;

public class TeacherDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvWelcome, tvCoursesCount, tvStudentsCount, tvPendingRequestsCount;
<<<<<<< HEAD
    private Button btnManageCourses, btnViewRequests, btnCreateQuiz, btnCreateCourse;
=======
    private Button btnManageCourses, btnViewRequests, btnViewStudents, btnCreateCourse;
>>>>>>> 542cd6292b68990ee1f3d7aca232549a251d4232
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RealtimeManager realtimeManager;
    private String currentTeacherId; // Đổi từ currentTeacherName sang currentTeacherId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        initViews();
        setupToolbar();
        initFirebase();
        loadDashboardData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWelcome = findViewById(R.id.tv_welcome);
        tvCoursesCount = findViewById(R.id.tv_courses_count);
        tvStudentsCount = findViewById(R.id.tv_students_count);
        tvPendingRequestsCount = findViewById(R.id.tv_pending_requests_count);

        btnManageCourses = findViewById(R.id.btn_manage_courses);
        btnViewRequests = findViewById(R.id.btn_view_requests);
<<<<<<< HEAD
        btnCreateQuiz = findViewById(R.id.btn_create_quiz);
=======
        btnViewStudents = findViewById(R.id.btn_view_students);
>>>>>>> 542cd6292b68990ee1f3d7aca232549a251d4232
        btnCreateCourse = findViewById(R.id.btn_create_course);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bảng điều khiển giáo viên");
        }
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        realtimeManager = RealtimeManager.getInstance();
    }

    private void setupClickListeners() {
        btnManageCourses.setOnClickListener(v -> {
            startActivity(new Intent(this, CourseManagementActivity.class));
        });

        // Thêm click listener cho nút "View Requests"
        btnViewRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, CourseRequestManagementActivity.class));
        });

<<<<<<< HEAD
        btnCreateQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, SelectCourseForQuizActivity.class));
=======
        btnViewStudents.setOnClickListener(v -> {
            startActivity(new Intent(this, EnrollmentManagementActivity.class));
>>>>>>> 542cd6292b68990ee1f3d7aca232549a251d4232
        });

        btnCreateCourse.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateCourseActivity.class));
        });
    }

    private void loadDashboardData() {
        String teacherId = mAuth.getCurrentUser().getUid();
        currentTeacherId = teacherId; // Lưu teacherId trực tiếp

        // Load teacher info
        db.collection("users").document(teacherId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        tvWelcome.setText("Xin chào, " + fullName);

                        // Load counts với teacherId
                        loadCoursesCount(teacherId);
                        loadStudentsCount(teacherId);
                        setupRealTimePendingRequestsCount(); // Sử dụng teacherId
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải thông tin", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCoursesCount(String teacherId) {
        db.collection("courses")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvCoursesCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    tvCoursesCount.setText("0");
                });
    }

    private void loadStudentsCount(String teacherId) {
        db.collection("enrollments")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvStudentsCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    tvStudentsCount.setText("0");
                });
    }

    // Sửa để hiển thị TẤT CẢ pending requests (không filter theo teacherId)
    private void setupRealTimePendingRequestsCount() {
        // Đơn giản hóa - đếm tất cả pending requests
        db.collection("courseRequests")
                .whereEqualTo("status", "pending")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("TeacherDashboard", "Error listening to pending requests", e);
                        tvPendingRequestsCount.setText("0");
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        int count = queryDocumentSnapshots.size();
                        Log.d("TeacherDashboard", "Real-time update: " + count + " pending requests total");

                        runOnUiThread(() -> {
                            tvPendingRequestsCount.setText(String.valueOf(count));

                            if (count > 0) {
                                btnViewRequests.setText("Xem yêu cầu (" + count + ")");
                                // Animation nhấp nháy khi có yêu cầu mới
                                btnViewRequests.animate()
                                        .scaleX(1.1f)
                                        .scaleY(1.1f)
                                        .setDuration(200)
                                        .withEndAction(() -> {
                                            btnViewRequests.animate()
                                                    .scaleX(1f)
                                                    .scaleY(1f)
                                                    .setDuration(200)
                                                    .start();
                                        })
                                        .start();
                            } else {
                                btnViewRequests.setText("Xem yêu cầu");
                            }
                        });
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teacher_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data khi quay lại activity
        loadDashboardData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup listeners khi destroy activity
        if (realtimeManager != null) {
            realtimeManager.removeAllListeners();
        }
    }
}
