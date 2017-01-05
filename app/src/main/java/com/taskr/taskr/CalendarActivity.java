package com.taskr.taskr;

import android.content.Intent;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.WeekViewLoader;
import com.taskr.taskr.models.Brain;
import com.taskr.taskr.models.OfflineDatabase;
import com.taskr.taskr.models.Schedule;
import com.taskr.taskr.models.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity implements MonthLoader.MonthChangeListener, WeekView.EventClickListener, WeekView.EventLongPressListener, WeekViewLoader {

    private WeekView mWeekView;
    private OfflineDatabase offlineDatabase;
    private Brain brain;
    private ArrayList<WeekViewEvent> events = new ArrayList<>();
    private int i = 0;
    private ArrayList<Task> splits = new ArrayList<>();
    private int day = 1;
    private int hour = 12;
    private ArrayList<WeekViewEvent> events1 = new ArrayList<>();
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        getSupportActionBar().setTitle("Schedule 1");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        brain = new Brain(1.0f, 1.0f, 1.0f, 1.0f);
        offlineDatabase = new OfflineDatabase();
//        events1.add(new WeekViewEvent(1, "Sleep", 2016, 12, 4, 22, 00, 2016, 12, 5, 6, 15));
//        events1.add(new WeekViewEvent(2, "Study for Physics final Part 1", 2016, 12, 1, 14, 0, 2016, 12, 1, 15, 0));
//        events1.add(new WeekViewEvent(3, "Study for Physics final Part 2", 2016, 12, 3, 14, 0, 2016, 12, 3, 15, 0));
//        events1.add(new WeekViewEvent(4, "Study for Physics final Part 3", 2016, 12, 5, 14, 0, 2016, 12, 5, 15, 0));

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);

        mWeekView.setWeekViewLoader(this);

        // Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);
    }

    private ArrayList<Task> splitTasks(ArrayList<Task> tasks, float timeInterval) {
        System.out.println("splitTasks");
        int hour = 12;
        int minutes = 0;
        ArrayList<Task> retTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getManual()) {
                retTasks.add(task);
            } else {
                float numTasks = brain.getNumParts(task, timeInterval);
                for (int n = 1; n < numTasks; n++) {
                    float duration = brain.getDuration(task, timeInterval, n);
                    Date startDate = new Date();
                    startDate.setHours(hour);
                    startDate.setMinutes(0);
                    startDate.setYear(2016-1900);
                    startDate.setDate(startDate.getDate());
                    Date endDate = new Date();
                    endDate.setYear(2016-1900);
                    endDate.setDate(startDate.getDate() + (n + 10));
                    endDate.setHours(hour + 1);
                    System.out.println(startDate.toString());
                    System.out.println(endDate.toString());
                    retTasks.add(new Task(task.getName(), startDate, endDate, false, task.getCompletion(), task.getNotes()));
                    hour++;
                }
            }
        }
        return retTasks;
    }

    @Override
    public void onResume() {
        super.onResume();
        i = 0;
        events1.clear();
        ArrayList<Task> tasks = brain.splitTasks(offlineDatabase.getAllTasks(), 1.0f);
        System.out.println("size: " + tasks.size());
        for (Task task : tasks) {
            events1.add(convert(task));
        }
        mWeekView.notifyDatasetChanged();
//        events.clear();
//        ArrayList<Task> tasks = offlineDatabase.getAutomaticTasks();
//        Date start = new Date();
//        start.setYear(2016);
//        Date end = start;
//        end.setDate(3);
//        Schedule schedule = brain.autoSchedule(start, end, tasks);
//        ArrayList<Task> tasks1 = schedule.getTasks();
//        for (Task task : tasks1) {
//            events.add(new WeekViewEvent(task.getId(), task.getName(), task.getStartDate().getYear(), task.getStartDate().getMonth(), task.getStartDate().getDay(), task.getStartDate().getHours(), task.getStartDate().getMinutes(), task.getEndDate().getYear(), task.getEndDate().getMonth(), task.getEndDate().getDay(), task.getEndDate().getHours(), task.getEndDate().getMinutes()));
//        }
//        mWeekView.getMonthChangeListener().onMonthChange(2016, 12);
//        ArrayList<Task> automaticTasks = offlineDatabase.getAutomaticTasks();
//        ArrayList<Task> manualTasks = offlineDatabase.getManualTasks();
//        ArrayList<Task> tasks = new ArrayList<>();
//        tasks.addAll(manualTasks);
//        tasks.addAll(automaticTasks);
//        splits = splitTasks(tasks, 1.0f);
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {

    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        System.out.println("onMonthChange");
        System.out.println(events1.size());
        if (i < 2) {
            //(long id, String name, int startYear, int startMonth, int startDay, int startHour, int startMinute, int endYear, int endMonth, int endDay, int endHour, int endMinute)
            i++;
            return new ArrayList<>();
        }
        for (WeekViewEvent event : events1) {
            System.out.println(event.getName() + "  " + event.getStartTime() + " " + event.getEndTime());
        }
        return events1;
    }

    public WeekViewEvent convert(Task task) {
        System.out.println("Convert: " + task.getName() + " " + task.getStartDate().toString() + " " + task.getEndDate().toString());
        System.out.println((task.getStartDate().getYear()+1900) + " " + task.getStartDate().getMonth() + " " + task.getStartDate().getDate() + " " + task.getStartDate().getHours() + " " + task.getStartDate().getMinutes() + " " + (task.getEndDate().getYear()+1900) + " " + task.getEndDate().getMonth() + " " + task.getEndDate().getDate() + " " + task.getEndDate().getHours() + " " + task.getEndDate().getMinutes());
        return new WeekViewEvent(task.getId(), task.getName(), task.getStartDate().getYear()+1900, task.getStartDate().getMonth()+1, task.getStartDate().getDate(), task.getStartDate().getHours(), task.getStartDate().getMinutes(), task.getEndDate().getYear()+1900, task.getEndDate().getMonth()+1, task.getEndDate().getDate(), task.getEndDate().getHours(), task.getEndDate().getMinutes());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_edit:
                startActivity(new Intent(CalendarActivity.this, MainActivity.class));
                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add:
//                if (count == 0) {
//                    events1.add(new WeekViewEvent(2, "Study for Math final Part 1", 2016, 12, day, 18, 0, 2016, 12, day, 20, 0));
//                    events1.add(new WeekViewEvent(3, "Study for Math final Part 2", 2016, 12, day + 1, 18, 0, 2016, 12, day + 1, 20, 0));
//                    events1.add(new WeekViewEvent(4, "Study for Math final Part 3", 2016, 12, day + 2, 18, 0, 2016, 12, day + 2, 20, 0));
//                } else if (count == 1) {
//                    events1.add(new WeekViewEvent(2, "Meeting with Mark", 2016, 12, day, 9, 0, 2016, 12, day, 10, 0));
//                } else if (count == 2) {
//                    events1.add(new WeekViewEvent(2, "Date night", 2016, 12, day, 20, 0, 2016, 12, day, 23, 0));
//                }
//                day++;
//                hour++;
//                count++;
//                i = 0;
//                mWeekView.notifyDatasetChanged();
                startActivity(new Intent(CalendarActivity.this, MainActivity.class));
                break;
        }
        return false;
    }

    @Override
    public double toWeekViewPeriodIndex(Calendar instance) {
        return 0;
    }

    @Override
    public List<? extends WeekViewEvent> onLoad(int periodIndex) {
        return null;
    }
}
