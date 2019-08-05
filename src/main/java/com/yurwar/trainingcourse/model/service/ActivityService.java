package com.yurwar.trainingcourse.model.service;

import com.yurwar.trainingcourse.model.dao.ActivityDao;
import com.yurwar.trainingcourse.model.dao.DaoFactory;
import com.yurwar.trainingcourse.model.entity.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class ActivityService {
    private final DaoFactory daoFactory = DaoFactory.getInstance();
    private static final Logger log = LogManager.getLogger();

    public List<Activity> getAllActivities() {
        try (ActivityDao activityDao = daoFactory.createActivityDao()) {
            return activityDao.findAll();
        } catch (Exception e) {
            log.warn("Can not get all users", e);
            return Collections.emptyList();
        }
    }

    public void createActivity(Activity activity) {
        try (ActivityDao activityDao = daoFactory.createActivityDao()) {
            activityDao.create(activity);
        } catch (Exception e) {
            log.warn("Can not create activity", e);
        }
    }

    public void deleteActivity(long activityId) {
        try (ActivityDao activityDao = daoFactory.createActivityDao()) {
            activityDao.delete(activityId);
        } catch (Exception e) {
            log.warn("Can not delete activity", e);
        }
    }
}
