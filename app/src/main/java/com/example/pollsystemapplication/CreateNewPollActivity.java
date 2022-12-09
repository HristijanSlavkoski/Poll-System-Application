package com.example.pollsystemapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CreateNewPollActivity extends AppCompatActivity {

    Button addQuestionButton, finishCreatingPoll;
    AlertDialog dialogForQuestion, dialogForAnswer;
    LinearLayout QuestionContainer;
    EditText pollTitleText;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_poll);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        addQuestionButton = findViewById(R.id.addQuestion);
        finishCreatingPoll = findViewById(R.id.finishCreatingPoll);
        QuestionContainer = findViewById(R.id.questionContainer);
        pollTitleText = findViewById(R.id.pollTitleText);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        buildDialogForQuestion();
        addQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogForQuestion.show();
            }
        });

        finishCreatingPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Please wait while we add the new poll");
                progressDialog.setTitle("New poll");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                try {
                    ArrayList<Question> questions = new ArrayList<>();
                    final int numberOfQuestions = QuestionContainer.getChildCount();
                    if (numberOfQuestions == 0) {
                        throw new Exception("Please add at least one question");
                    }
                    for (int i = 0; i < numberOfQuestions; i++) {
                        View questionView = QuestionContainer.getChildAt(i);
                        TextView questionName = questionView.findViewById(R.id.question);
                        LinearLayout answerLayout = questionView.findViewById(R.id.answerLayout);
                        ArrayList<String> answers = new ArrayList<>();
                        final int numberOfAnswers = answerLayout.getChildCount();
                        if (numberOfAnswers == 0) {
                            throw new Exception("Please add at least one choice to each question");
                        }
                        for (int j = 0; j < numberOfAnswers; j++) {
                            TextView answerName = answerLayout.findViewById(R.id.answer);
                            answers.add(answerName.getText().toString());
                        }
                        questions.add(new Question(questionName.getText().toString(), answers));
                    }
                    //On create, all polls will be inactive, we can activate them later
                    Poll poll = new Poll(pollTitleText.getText().toString(), firebaseUser.getEmail(), questions, false);
                    databaseReference.child("poll").push().setValue(poll).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(CreateNewPollActivity.this, AdministratorHomePage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                Toast.makeText(CreateNewPollActivity.this, "New poll created successfuly", Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(CreateNewPollActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(CreateNewPollActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void buildDialogForQuestion() {
        AlertDialog.Builder builderForQuestion = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.create_new_question_dialog, null);

        EditText questionName = view.findViewById(R.id.nameEdit);
        builderForQuestion.setView(view);
        builderForQuestion.setTitle("Enter new question")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        addQuestion(questionName.getText().toString());
                        questionName.getText().clear();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        dialogForQuestion = builderForQuestion.create();
    }

    private void addQuestion(String questionName) {
        View view = getLayoutInflater().inflate(R.layout.activity_creating_questions_card, null);
        TextView nameView = view.findViewById(R.id.question);
        Button deleteQuestion = view.findViewById(R.id.deleteQuestion);
        Button addAnswers = view.findViewById(R.id.addAnswers);
        nameView.setText(questionName);
        LinearLayout answerContainer = view.findViewById(R.id.answerLayout);
        deleteQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionContainer.removeView(view);
                QuestionContainer.forceLayout();
            }
        });
        addAnswers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildDialogForAnswer(answerContainer);
                dialogForAnswer.show();
            }
        });
        QuestionContainer.addView(view);
    }

    private void buildDialogForAnswer(LinearLayout answerContainer) {
        AlertDialog.Builder builderForAnswer = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.create_new_answer_dialog, null);

        EditText answerName = view.findViewById(R.id.nameEditAnswer);

        builderForAnswer.setView(view);
        builderForAnswer.setTitle("Enter new choice")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        addAnswer(answerContainer, answerName.getText().toString());
                        answerName.getText().clear();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        dialogForAnswer = builderForAnswer.create();
    }

    private void addAnswer(LinearLayout answerContainer, String answerName) {
        View view = getLayoutInflater().inflate(R.layout.activity_creating_answer_card, null);
        TextView nameView = view.findViewById(R.id.answer);
        Button deleteAnswer = view.findViewById(R.id.deleteAnswer);
        nameView.setText(answerName);

        deleteAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerContainer.removeView(view);
                answerContainer.forceLayout();
            }
        });
        answerContainer.addView(view);
    }
}