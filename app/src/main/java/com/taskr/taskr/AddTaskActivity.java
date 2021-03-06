package com.taskr.taskr;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.taskr.taskr.models.Database;
import com.taskr.taskr.models.OfflineDatabase;
import com.taskr.taskr.models.Task;

import java.util.Date;

import static com.taskr.taskr.Globals.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL;

public class AddTaskActivity extends AppCompatActivity {

    private EditText taskName;
    private EditText hoursNeeded;

    private TextView importanceHeading;
    private SeekBar importanceBar;

    private TextView desirabilityHeading;
    private SeekBar desirabilityBar;

    private TextView urgencyHeading;

    private String date;
    private int month;
    private int day;
    private int year;

    private String time;
    private int hour;
    private int minute;

    private EditText notes;

    private Switch manualSwitch;

    private TextView endTimeTxt;
    private TextView endDate;
    private TextView endTime;
    private Button endDateBtn;
    private Button endTimeBtn;

    private String endDateStr;
    private int endMonth;
    private int endDay;
    private int endYear;

    private String endTimeStr;
    private int endHour;
    private int endMinute;

    private boolean allowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        taskName = (EditText) findViewById(R.id.taskName);
        hoursNeeded = (EditText) findViewById(R.id.taskTime);
        notes = (EditText) findViewById(R.id.notes);

        endTimeTxt = (TextView) findViewById(R.id.endTimeTxt);
        endDate = (TextView) findViewById(R.id.endDate);
        endTime = (TextView) findViewById(R.id.endTime);

        endDateBtn = (Button) findViewById(R.id.endDateBtn);
        endDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dateDialog = new AlertDialog.Builder(AddTaskActivity.this);
                final View dateView = getLayoutInflater().inflate(R.layout.date_picker, null, false);
                dateDialog.setView(dateView);
                dateDialog.setTitle("Due Date");
                dateDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatePicker dp = (DatePicker) dateView.findViewById(R.id.datePicker);
                        endMonth = dp.getMonth();
                        int tempMonth = endMonth + 1;
                        endDay = dp.getDayOfMonth();
                        endYear = dp.getYear();
                        endDateStr = tempMonth + "/" + dp.getDayOfMonth() + "/" + dp.getYear();
                        endDate.setText(endDateStr);

                    }
                });
                dateDialog.show();
            }
        });
        endTimeBtn = (Button) findViewById(R.id.endTimeBtn);
        endTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder timeDialog = new AlertDialog.Builder(AddTaskActivity.this);
                final View timeView = getLayoutInflater().inflate(R.layout.time_picker, null, false);
                timeDialog.setView(timeView);
                timeDialog.setTitle("Due Time");
                timeDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TimePicker tp = (TimePicker) timeView.findViewById(R.id.timePicker);
                        endHour = tp.getHour();
                        endMinute = tp.getMinute();
                        endTimeStr = tp.getHour() + ":"+ (tp.getMinute() < 10 ? "0"+tp.getMinute() : tp.getMinute());
                        endTime.setText(endTimeStr);
                    }
                });
                timeDialog.show();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(manualSwitch.isChecked()) {
                    Snackbar.make(view, "Manual", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    if (taskName.getText().toString().trim().isEmpty() ||
                            notes.getText().toString().trim().isEmpty() ||
                            endDateStr == null ||
                            endTimeStr == null ||
                            date == null ||
                            time == null) {
                        Snackbar.make(view, "Cannot leave field(s) empty", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    } else {
                        Date currDate = new Date();
                        Date startDate = new Date(year-1900, month, day, hour, minute);
                        Date endDate = new Date(endYear-1900, endMonth, endDay, endHour, endMinute);
                        System.out.println("startDate.getTime(): " + startDate.getTime());
                        System.out.println("currDate.getTime(): " + currDate.getTime());
                        System.out.println("endDate.getTime(): " + endDate.getTime());
                        System.out.println("startDate.toString(): " + startDate.toString());
                        System.out.println("currDate.toString(): " + currDate.toString());
                        System.out.println("endDate.toString(): " + endDate.toString());
                        if (endDate.getTime() - startDate.getTime() <= 0 || startDate.getTime() - currDate.getTime() <= 0) {
                            Snackbar.make(view, "Invalid date range", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        } else {
                            Task task = new Task(taskName.getText().toString(), startDate, endDate, true, Float.valueOf(0), notes.getText().toString());
                            OfflineDatabase offlineDatabase = new OfflineDatabase();
                            offlineDatabase.addManualTask(task);
                            Intent intent = new Intent();
                            intent.putExtra(Globals.TASK, task);
                            setResult(Globals.RESULT_TASK_CREATED, intent);
                            finish();
                        }
                    }
                } else {
                    Snackbar.make(view, "Automatic", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    if (taskName.getText().toString().trim().isEmpty() ||
                            hoursNeeded.getText().toString().trim().isEmpty() ||
                            notes.getText().toString().trim().isEmpty() ||
                            date == null ||
                            time == null) {
                        Snackbar.make(view, "Cannot leave field(s) empty", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    } else {
                        Date currDate = new Date();
                        Date urgency = new Date(year-1900, month, day, hour, minute);
                        System.out.println("urgency.getTime(): " + urgency.getTime());
                        System.out.println("currDate.getTime(): " + currDate.getTime());
                        System.out.println("urgency.toString(): " + urgency.toString());
                        System.out.println("currDate.toString(): " + currDate.toString());
                        if (urgency.getTime() - currDate.getTime() <= 0) {
                            Snackbar.make(view, "Invalid date", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        } else {
                            Task task = new Task(taskName.getText().toString(), Float.valueOf(hoursNeeded.getText().toString()), Float.valueOf(desirabilityBar.getProgress()), urgency, Float.valueOf(importanceBar.getProgress()), false, Float.valueOf(0), notes.getText().toString());

                            OfflineDatabase offlineDatabase = new OfflineDatabase();
                            offlineDatabase.addAutoTask(task);
//                            if (r != -1) {
//                                Toast.makeText(AddTaskActivity.this, "Success: " + r, Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(AddTaskActivity.this, "Failure", Toast.LENGTH_SHORT).show();
//                            }
                            Intent intent = new Intent();
                            intent.putExtra(Globals.TASK, task);
                            setResult(Globals.RESULT_TASK_CREATED, intent);
                            finish();
                        }
                    }
                }

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        desirabilityHeading = (TextView) findViewById(R.id.taskDesirabilityHeading);
        desirabilityBar = (SeekBar) findViewById(R.id.taskDesirability);

        desirabilityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress + 1;
                desirabilityHeading.setText("Desirability: " + progressChanged);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                desirabilityHeading.setText("Desirability: " + progressChanged);
            }
        });
        importanceHeading = (TextView) findViewById(R.id.taskImportanceHeading);
        importanceBar = (SeekBar) findViewById(R.id.taskImportance);

        importanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress + 1;
                importanceHeading.setText("Importance: " + progressChanged);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                importanceHeading.setText("Importance: " + progressChanged);
            }
        });

        urgencyHeading = (TextView) findViewById(R.id.taskUrgencyHeading);

        manualSwitch = (Switch) findViewById(R.id.switch1);
        manualSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleManualUI();
            }
        });


        final TextView dateTxt = (TextView) findViewById(R.id.dateTxt);
        final TextView timeTxt = (TextView) findViewById(R.id.timeTxt);

        Button dateBtn = (Button) findViewById(R.id.dateBtn);
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dateDialog = new AlertDialog.Builder(AddTaskActivity.this);
                final View dateView = getLayoutInflater().inflate(R.layout.date_picker, null, false);
                dateDialog.setView(dateView);
                dateDialog.setTitle("Due Date");
                dateDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatePicker dp = (DatePicker) dateView.findViewById(R.id.datePicker);
                        month = dp.getMonth();
                        int tempMonth = month + 1;
                        day = dp.getDayOfMonth();
                        year = dp.getYear();
                        date = tempMonth + "/" + dp.getDayOfMonth() + "/" + dp.getYear();
                        dateTxt.setText(date);

                    }
                });
                dateDialog.show();
            }
        });

        Button timeBtn = (Button) findViewById(R.id.timebtn);
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder timeDialog = new AlertDialog.Builder(AddTaskActivity.this);
                final View timeView = getLayoutInflater().inflate(R.layout.time_picker, null, false);
                timeDialog.setView(timeView);
                timeDialog.setTitle("Due Time");
                timeDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TimePicker tp = (TimePicker) timeView.findViewById(R.id.timePicker);
                        hour = tp.getHour();
                        minute = tp.getMinute();
                        time = tp.getHour() + ":"+ (tp.getMinute() < 10 ? "0"+tp.getMinute() : tp.getMinute());
                        timeTxt.setText(time);
                    }
                });
                timeDialog.show();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    allowed = true;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    allowed = false;

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void toggleManualUI() {
        if(!manualSwitch.isChecked()) {
            importanceHeading.setVisibility(View.VISIBLE);
            importanceBar.setVisibility(View.VISIBLE);

            desirabilityHeading.setVisibility(View.VISIBLE);
            desirabilityBar.setVisibility(View.VISIBLE);

            hoursNeeded.setVisibility(View.VISIBLE);
            urgencyHeading.setText("Deadline:");

            endTime.setVisibility(View.GONE);
            endTimeTxt.setVisibility(View.GONE);
            endTimeBtn.setVisibility(View.GONE);
            endDate.setVisibility(View.GONE);
            endDateBtn.setVisibility(View.GONE);

        } else {
            importanceHeading.setVisibility(View.GONE);
            importanceBar.setVisibility(View.GONE);

            desirabilityHeading.setVisibility(View.GONE);
            desirabilityBar.setVisibility(View.GONE);

            hoursNeeded.setVisibility(View.GONE);

            urgencyHeading.setText("Start Time:");

            endTime.setVisibility(View.VISIBLE);
            endTimeTxt.setVisibility(View.VISIBLE);
            endTimeBtn.setVisibility(View.VISIBLE);
            endDate.setVisibility(View.VISIBLE);
            endDateBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return false;
    }

}
