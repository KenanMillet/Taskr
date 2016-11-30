package com.taskr.taskr.models;

/**
 * Created by Kenan Millet on 11/27/2016.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Brain {
    public Brain(final float des, final float imp, final float dur, final float urg) {
        this.desWeight = des;
        this.impWeight = imp;
        this.durWeight = dur;
        this.urgWeight = urg;
    }

    public Schedule autoSchedule(Date begin, Date end, ArrayList<Task> tasks) {
        return autoSchedule(begin, end, tasks, 1.0f);
    }
    public Schedule autoSchedule(Date begin, Date end, ArrayList<Task> tasks, float timeInterval) {
        final Schedule schedule = new Schedule("Taskr Auto-Generated Schedule", begin, end);

        for(Task t : tasks) //split up auto tasks into pieces no greater than timeInterval and add to schedule
        {
            if(t.getManual()) schedule.addTask(t);
            else
            {
                int nparts = (int) (timeNeeded(t) / timeInterval) + ((timeNeeded(t) % timeInterval == 0.0f) ? 0 : 1);
                float comp = t.getCompletion(); //store original completion since we will be modifying it
                for (int n = 1; n <= nparts; ++n) {
                    //calculate how long the task interval will be in the range (0-timeInterval]
                    float time = ((timeNeeded(t) - ((float) n) * timeInterval < 0.0f) ? timeNeeded(t) : timeInterval);
                    //add the split up task interval to the schedule task list
                    schedule.addTask(new Task(t.getName() + String.format(" (part %1$d of %2$d)", n, nparts), time, t.getDesirability(), t.getUrgency(), t.getImportance(), t.getManual(), t.getCompletion(), t.getNotes()));
                    t.setCompletion(t.getCompletion() + (time / t.getDuration())); //add to completion the % we accomplished with this time interval
                }
                t.setCompletion(comp);
            }
        }

        //now that all tasks have been split up and/or added to the schedule,
        //sort schedule task list by priority
        Collections.sort(schedule.getTasks(), new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) { return Float.compare(getPriority(schedule.getTasks().indexOf(o2), schedule.getTasks()), getPriority(schedule.getTasks().indexOf(o1), schedule.getTasks())); }
        });


        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(begin);
        int beginDay = cal.get(Calendar.DAY_OF_YEAR);
        Date beginTime = cal.getTime();
        int days = (int)(begin.getTime() - end.getTime())/(1000 * 60 * 60 * 24);

        //fill up time slots with tasks
        for(int i = 0; i < schedule.getTasks().size(); ++i)
        {
            Task t = schedule.getTasks().get(i);
            if(t.getManual())
            {
                cal.setTime(t.getStartDate());
                int day = cal.get(Calendar.DAY_OF_YEAR) - beginDay;
                float start = ((float)(100 * cal.get(Calendar.HOUR_OF_DAY))) + (((float)cal.get(Calendar.MINUTE))/0.6f);
                cal.setTime(t.getEndDate());
                float dura = ((float)(100 * cal.get(Calendar.HOUR_OF_DAY))) + (((float)cal.get(Calendar.MINUTE))/0.6f) - start;

                if(dura < 0) dura += 2400.0f;
                if(day < 0) day += 365 + ((cal.isLeapYear(cal.get(Calendar.YEAR)- 1)) ? 1 : 0);
                t.setStart(start);
                t.setDuration(dura);

                schedule.setTaskDay(i, day);
            }
            else
            {
                for(int d = 0; d < days; ++d)
                {
                    for(int s = 0; s < 2400; ++s)
                    {
                        if(schedule.isTimeslotFree(d, s, t.getDuration()*100.0f))
                        {
                            t.setStart(s);
                            t.setDuration(t.getDuration()*100.0f);
                            schedule.setTaskDay(i, d);
                        }
                    }
                }
            }
        }

        DateFormat boundformat = new SimpleDateFormat("[dd/MM/yyyy]");
        DateFormat timestamp = new SimpleDateFormat("[dd/MM/yyyy hh:mm:ss a].");

        schedule.setNotes("Taskr thinks this schedule is optimal for your work habits.\nSchedule from " + boundformat.format(begin) + " to " + boundformat.format(end) + " automatically generated by Taskr on " + timestamp.format(new Date()));
        return schedule;
    }



    private float desWeight;
    private float impWeight;
    private float durWeight;
    private float urgWeight;

    private float timeRemaining(Task t) {
        return timeRemaining(t, new Date());
    }
    private float timeRemaining(Task t, Date refTime) {
        return (float) (((double)(t.getUrgency().getTime() - refTime.getTime()))/(60.0*60.0*1000.0));
    }

    private float timeNeeded(Task t) {
        return timeNeeded(t, 0.0f);
    }
    private float timeNeeded(Task t, float offset) {
        return (1.0f - (t.getCompletion() + offset)) * t.getDuration();
    }

    private float getTimeAvailability(int index, ArrayList<Task> tasks) {
        return getTimeAvailability(index, tasks, 0.0f, new Date());
    }
    private float getTimeAvailability(int index, ArrayList<Task> tasks, float offset, Date refTime) {
        float totalOccupancy = 0.0f;
        ArrayList<Task> tasklist = new ArrayList(tasks);
        Collections.sort(tasklist, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) { return o1.getUrgency().compareTo(o2.getUrgency()); }
        });
        List<Task> tlist = tasklist.subList(0, index);
        for(Task t : tasklist) { totalOccupancy += timeNeeded(t, offset); }

        return timeRemaining(tasks.get(index), refTime) - totalOccupancy;
    }

    private float getPriority(int index, ArrayList<Task> tasks) {
        return getPriority(index, tasks, 0.0f, new Date());
    }
    private float getPriority(int index, ArrayList<Task> tasks, float offset, Date refTime) {
        Task t = tasks.get(index);
        if(t.getManual()) return 2.0f;

        final float des = t.getDesirability();
        final float imp = t.getImportance();
        final double time = urgWeight * getTimeAvailability(index, tasks, offset, refTime);
        final double c = 1.0-(t.getCompletion() + offset);
        final double x = ((time > 0) ? (durWeight * timeNeeded(t, offset))/time : 1.0f);
        final double p0 = des * desWeight * c / 100.0;
        final double p1 = 1.0 - Math.pow(1.0 - (des*c/10.0), desWeight);
        final double p2 = 1.0 - Math.pow(1.0 - (imp*c/10.0), impWeight);
        final double p3 = 1.0;

        return (float)((x < 1.0) ? ((Math.pow(1.0-x, 3.0) * p0) + (Math.pow(1.0-x, 2.0) * x * 3.0 * p1) + ((1.0-x) * Math.pow(x, 2.0) * 3.0 * p2) + (Math.pow(x, 3.0) * p3)) : 1.0);
    }
}
