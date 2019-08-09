package com.yurwar.trainingcourse.model.dao.impl;

import com.yurwar.trainingcourse.model.dao.UserDao;
import com.yurwar.trainingcourse.model.dao.impl.mapper.ActivityMapper;
import com.yurwar.trainingcourse.model.dao.impl.mapper.ActivityRequestMapper;
import com.yurwar.trainingcourse.model.dao.impl.mapper.UserMapper;
import com.yurwar.trainingcourse.model.entity.Activity;
import com.yurwar.trainingcourse.model.entity.ActivityRequest;
import com.yurwar.trainingcourse.model.entity.Authority;
import com.yurwar.trainingcourse.model.entity.User;
import com.yurwar.trainingcourse.util.exception.DaoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

//TODO Add log
public class JDBCUserDao implements UserDao {
    public static final Logger log = LogManager.getLogger();
    private final Connection connection;
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("database");

    JDBCUserDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(User entity) {
        try (PreparedStatement userPS =
                     connection.prepareStatement(resourceBundle.getString("query.user.create"), Statement.RETURN_GENERATED_KEYS)) {
            userPS.setString(1, entity.getFirstName());
            userPS.setString(2, entity.getLastName());
            userPS.setString(3, entity.getPassword());
            userPS.setString(4, entity.getUsername());
            userPS.executeUpdate();
            ResultSet rs = userPS.getGeneratedKeys();

            if (rs.next()) {
                entity.setId(rs.getLong(1));
            }
            try (PreparedStatement authorityPS = connection.prepareStatement("insert into user_authorities (user_id, authorities) values (?, ?)")) {
                for (Authority authority : entity.getAuthorities()) {
                    authorityPS.setLong(1, entity.getId());
                    authorityPS.setString(2, authority.name());
                    authorityPS.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Can not create user", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (PreparedStatement ps = connection.prepareStatement("select " +
                "       users.id                     as \"users.id\",\n" +
                "       users.first_name             as \"users.first_name\",\n" +
                "       users.last_name              as \"users.last_name\",\n" +
                "       users.password               as \"users.password\",\n" +
                "       users.username               as \"users.username\"\n" +
                "from users\n" +
                "where users.username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            Map<Long, User> userMap = extractUsersFromResultSet(rs);

            return userMap.values().stream().findAny();
        } catch (SQLException e) {
            throw new DaoException("Can not find user by username", e);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        try (PreparedStatement ps = connection.prepareStatement("select " +
                "       users.id                     as \"users.id\",\n" +
                "       users.first_name             as \"users.first_name\",\n" +
                "       users.last_name              as \"users.last_name\",\n" +
                "       users.password               as \"users.password\",\n" +
                "       users.username               as \"users.username\"\n" +
                "from users\n" +
                "where users.id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            Map<Long, User> userMap = extractUsersFromResultSet(rs);

            return userMap.values().stream().findAny();
        } catch (SQLException e) {
            throw new DaoException("Can not find user by id", e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery("select " +
                    "       users.id                     as \"users.id\",\n" +
                    "       users.first_name             as \"users.first_name\",\n" +
                    "       users.last_name              as \"users.last_name\",\n" +
                    "       users.password               as \"users.password\",\n" +
                    "       users.username               as \"users.username\"\n" +
                    "from users");

            Map<Long, User> userMap = extractUsersFromResultSet(rs);

            return new ArrayList<>(userMap.values());
        } catch (SQLException e) {
            throw new DaoException("Can not find all users", e);
        }
    }

    @Override
    public List<User> findAllPageable(int page, int size) {
        try (PreparedStatement ps = connection.prepareStatement("select " +
                "       users.id                     as \"users.id\",\n" +
                "       users.first_name             as \"users.first_name\",\n" +
                "       users.last_name              as \"users.last_name\",\n" +
                "       users.password               as \"users.password\",\n" +
                "       users.username               as \"users.username\"\n" +
                "from users\n" +
                "order by users.last_name, users.first_name\n" +
                "limit ? offset ?")) {
            connection.setAutoCommit(false);
            ps.setLong(1, size);
            ps.setLong(2, size * page);
            ResultSet rs = ps.executeQuery();

            Map<Long, User> userMap = extractUsersFromResultSet(rs);

            connection.commit();
            return new ArrayList<>(userMap.values());
        } catch (SQLException e) {
            throw new DaoException("Can not find all users", e);
        }
    }

    @Override
    public void update(User entity) {
        try (PreparedStatement userPS = connection.prepareStatement("update users set first_name = ?, last_name = ?, password = ?, username = ? where id = ?")) {
            userPS.setString(1, entity.getFirstName());
            userPS.setString(2, entity.getLastName());
            userPS.setString(3, entity.getPassword());
            userPS.setString(4, entity.getUsername());
            userPS.setLong(5, entity.getId());
            userPS.executeUpdate();

            try (PreparedStatement authorityDeletePS = connection.prepareStatement("delete from user_authorities where user_id = ?")) {
                authorityDeletePS.setLong(1, entity.getId());
                authorityDeletePS.executeUpdate();
            }
            try (PreparedStatement authorityInsertPS = connection.prepareStatement("insert into user_authorities values (?, ?)")) {
                for (Authority authority : entity.getAuthorities()) {
                    authorityInsertPS.setLong(1, entity.getId());
                    authorityInsertPS.setString(2, authority.name());
                    authorityInsertPS.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Can not update user", e);
        }
    }

    @Override
    public void delete(long id) {
        try (PreparedStatement ps = connection.prepareStatement(resourceBundle.getString("query.user.delete"))) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public long getNumberOfRecords() {
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery("select count(*) from users");

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Can not get numbers of records", e);
        }
    }

    private Map<Long, User> extractUsersFromResultSet(ResultSet rs) throws SQLException {
        Map<Long, User> userMap = new LinkedHashMap<>();
        Map<Long, Activity> activityMap = new HashMap<>();
        Map<Long, ActivityRequest> activityRequestMap = new HashMap<>();

        UserMapper userMapper = new UserMapper();
        ActivityMapper activityMapper = new ActivityMapper();
        ActivityRequestMapper activityRequestMapper = new ActivityRequestMapper();

        while (rs.next()) {
            User user = userMapper.extractFromResultSet(rs);
            userMapper.makeUnique(userMap, user);
        }

        for (User user : userMap.values()) {
            try (PreparedStatement userAuthoritiesPS = connection.prepareStatement("select " +
                    "       users.id                     as \"users.id\",\n" +
                    "       user_authorities.user_id     as \"user_authorities.user_id\",\n" +
                    "       user_authorities.authorities as \"user_authorities.authorities\"\n" +
                    "from users\n" +
                    "         left join user_authorities on users.id = user_authorities.user_id\n " +
                    "where users.id = ?")) {
                userAuthoritiesPS.setLong(1, user.getId());
                ResultSet userAuthoritiesResultSet = userAuthoritiesPS.executeQuery();

                while (userAuthoritiesResultSet.next()) {
                    Authority authority = Authority
                            .valueOf(userAuthoritiesResultSet.getString("user_authorities.authorities"));
                    user.getAuthorities().add(authority);
                }
            }
            try (PreparedStatement userActivitiesPS = connection.prepareStatement("select " +
                    "       users.id                     as \"users.id\",\n" +
                    "       users_activities.user_id     as \"users_activities.user_id\",\n" +
                    "       users_activities.activity_id as \"users_activities.activity_id\",\n" +
                    "       activities.id                as \"activities.id\",\n" +
                    "       activities.name              as \"activities.name\",\n" +
                    "       activities.description       as \"activities.description\",\n" +
                    "       activities.start_time        as \"activities.start_time\",\n" +
                    "       activities.end_time          as \"activities.end_time\",\n" +
                    "       activities.duration          as \"activities.duration\",\n" +
                    "       activities.importance        as \"activities.importance\",\n" +
                    "       activities.status            as \"activities.status\"\n" +
                    "from users\n" +
                    "         left join users_activities on users.id = users_activities.user_id\n" +
                    "         left join activities on users_activities.activity_id = activities.id " +
                    "where users.id = ?")) {
                userActivitiesPS.setLong(1, user.getId());
                ResultSet userActivitiesResultSet = userActivitiesPS.executeQuery();

                while (userActivitiesResultSet.next()) {
                    Activity activity = activityMapper.extractFromResultSet(userActivitiesResultSet);
                    activity = activityMapper.makeUnique(activityMap, activity);

                    if (activity.getId() != 0 && !user.getActivities().contains(activity)) {
                        user.getActivities().add(activity);
                    }
                }
            }
            try (PreparedStatement activityRequestsPS = connection.prepareStatement("select " +
                    "       users.id                       as \"users.id\",\n" +
                    "       activity_requests.id           as \"activity_requests.id\",\n" +
                    "       activity_requests.activity_id  as \"activity_requests.activity_id\",\n" +
                    "       activity_requests.user_id      as \"activity_requests.user_id\",\n" +
                    "       activity_requests.request_date as \"activity_requests.request_date\",\n" +
                    "       activity_requests.action       as \"activity_requests.action\",\n" +
                    "       activity_requests.status       as \"activity_requests.status\",\n" +
                    "       activities.id                  as \"activities.id\",\n" +
                    "       activities.name                as \"activities.name\",\n" +
                    "       activities.description         as \"activities.description\",\n" +
                    "       activities.start_time          as \"activities.start_time\",\n" +
                    "       activities.end_time            as \"activities.end_time\",\n" +
                    "       activities.duration            as \"activities.duration\",\n" +
                    "       activities.importance          as \"activities.importance\",\n" +
                    "       activities.status              as \"activities.status\"\n" +
                    "from users\n" +
                    "         left join activity_requests on users.id = activity_requests.user_id\n" +
                    "         left join activities on activity_requests.activity_id = activities.id\n" +
                    "where users.id = ?")) {
                activityRequestsPS.setLong(1, user.getId());
                ResultSet userActivityRequestsResultSet = activityRequestsPS.executeQuery();

                while (userActivityRequestsResultSet.next()) {
                    ActivityRequest activityRequest = activityRequestMapper.extractFromResultSet(userActivityRequestsResultSet);
                    Activity activity = activityMapper.extractFromResultSet(userActivityRequestsResultSet);

                    activityRequest = activityRequestMapper.makeUnique(activityRequestMap, activityRequest);
                    activity = activityMapper.makeUnique(activityMap, activity);

                    if ((activityRequest.getId() != 0) && !user.getActivityRequests().contains(activityRequest)) {
                        activityRequest.setUser(user);
                        activityRequest.setActivity(activity);
                        user.getActivityRequests().add(activityRequest);
                    }
                }
            }
        }
        return userMap;
    }
}
