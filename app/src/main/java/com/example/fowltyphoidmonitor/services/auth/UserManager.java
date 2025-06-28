package com.example.fowltyphoidmonitor.services.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Comprehensive user management system for admin
 * Handles user creation, modification, deletion, and role management
 */
public class UserManager {
    private static final String TAG = "UserManager";
    private static final String PREFS_NAME = "FowlTyphoidMonitorUserManagement";
    private static final String KEY_USERS_LIST = "usersList";
    private static final String KEY_USER_ROLES = "userRoles";
    private static final String KEY_USER_PERMISSIONS = "userPermissions";

    private Context context;
    private Gson gson;
    private List<UserAccount> users;
    private List<UserManagerListener> listeners;

    // Singleton instance
    private static UserManager instance;

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }

    private UserManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.users = new ArrayList<>();
        this.listeners = new ArrayList<>();
        loadUsers();
    }

    /**
     * Interface for user management callbacks
     */
    public interface UserManagerListener {
        void onUserAdded(UserAccount user);
        void onUserUpdated(UserAccount user);
        void onUserDeleted(String userId);
        void onUserStatusChanged(String userId, UserStatus status);
        void onError(String error);
    }

    /**
     * User account data model
     */
    public static class UserAccount {
        public String userId;
        public String username;
        public String email;
        public String fullName;
        public String phoneNumber;
        public String location;
        public UserRole role;
        public UserStatus status;
        public Date dateCreated;
        public Date lastLogin;
        public String profileImageUrl;
        public UserPermissions permissions;
        public UserStatistics statistics;

        public UserAccount() {
            this.userId = UUID.randomUUID().toString();
            this.dateCreated = new Date();
            this.status = UserStatus.ACTIVE;
            this.permissions = new UserPermissions();
            this.statistics = new UserStatistics();
        }

        public UserAccount(String username, String email, String fullName, UserRole role) {
            this();
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
        }
    }

    /**
     * User roles enumeration
     */
    public enum UserRole {
        FARMER("Mfugaji"),
        VETERINARIAN("Daktari wa Mifugo"),
        ADMIN("Msimamizi"),
        SUPERVISOR("Msimamizi Mkuu"),
        FIELD_OFFICER("Afisa wa Ushamba");

        private final String swahiliName;

        UserRole(String swahiliName) {
            this.swahiliName = swahiliName;
        }

        public String getSwahiliName() {
            return swahiliName;
        }
    }

    /**
     * User status enumeration
     */
    public enum UserStatus {
        ACTIVE("Amilifu"),
        INACTIVE("Haijafanya kazi"),
        SUSPENDED("Amesimamishwa"),
        PENDING_APPROVAL("Inasubiri idhini"),
        BLOCKED("Amezuiliwa");

        private final String swahiliName;

        UserStatus(String swahiliName) {
            this.swahiliName = swahiliName;
        }

        public String getSwahiliName() {
            return swahiliName;
        }
    }

    /**
     * User permissions
     */
    public static class UserPermissions {
        public boolean canViewReports = true;
        public boolean canCreateReports = true;
        public boolean canEditReports = false;
        public boolean canDeleteReports = false;
        public boolean canManageUsers = false;
        public boolean canSendAlerts = false;
        public boolean canViewAnalytics = true;
        public boolean canManageSystem = false;

        public UserPermissions() {}

        public UserPermissions(UserRole role) {
            setPermissionsForRole(role);
        }

        private void setPermissionsForRole(UserRole role) {
            switch (role) {
                case FARMER:
                    canViewReports = true;
                    canCreateReports = true;
                    canEditReports = false;
                    canDeleteReports = false;
                    canManageUsers = false;
                    canSendAlerts = false;
                    canViewAnalytics = false;
                    canManageSystem = false;
                    break;

                case VETERINARIAN:
                    canViewReports = true;
                    canCreateReports = true;
                    canEditReports = true;
                    canDeleteReports = false;
                    canManageUsers = false;
                    canSendAlerts = true;
                    canViewAnalytics = true;
                    canManageSystem = false;
                    break;

                case FIELD_OFFICER:
                    canViewReports = true;
                    canCreateReports = true;
                    canEditReports = true;
                    canDeleteReports = true;
                    canManageUsers = false;
                    canSendAlerts = true;
                    canViewAnalytics = true;
                    canManageSystem = false;
                    break;

                case ADMIN:
                case SUPERVISOR:
                    canViewReports = true;
                    canCreateReports = true;
                    canEditReports = true;
                    canDeleteReports = true;
                    canManageUsers = true;
                    canSendAlerts = true;
                    canViewAnalytics = true;
                    canManageSystem = true;
                    break;
            }
        }
    }

    /**
     * User statistics
     */
    public static class UserStatistics {
        public int totalReports = 0;
        public int totalConsultations = 0;
        public int totalAlertsSent = 0;
        public Date lastActivity;
        public int loginCount = 0;
        public int averageResponseTime = 0; // in minutes

        public UserStatistics() {
            this.lastActivity = new Date();
        }
    }

    /**
     * Add user management listener
     */
    public void addUserManagerListener(UserManagerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove user management listener
     */
    public void removeUserManagerListener(UserManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Create new user account
     */
    public boolean createUser(String username, String email, String fullName,
                              String phoneNumber, String location, UserRole role) {
        try {
            // Check if username or email already exists
            if (getUserByUsername(username) != null) {
                notifyError("Jina la mtumiaji tayari lipo");
                return false;
            }

            if (getUserByEmail(email) != null) {
                notifyError("Barua pepe tayari imetumiwa");
                return false;
            }

            UserAccount newUser = new UserAccount(username, email, fullName, role);
            newUser.phoneNumber = phoneNumber;
            newUser.location = location;
            newUser.permissions = new UserPermissions(role);

            users.add(newUser);
            saveUsers();

            // Notify listeners
            for (UserManagerListener listener : listeners) {
                listener.onUserAdded(newUser);
            }

            Log.d(TAG, "User created successfully: " + username);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error creating user: " + e.getMessage());
            notifyError("Kosa katika kuunda mtumiaji: " + e.getMessage());
            return false;
        }
    }

    /**
     * Save users to SharedPreferences
     */
    private void saveUsers() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String usersJson = gson.toJson(users);

            prefs.edit()
                    .putString(KEY_USERS_LIST, usersJson)
                    .apply();

            Log.d(TAG, "Saved " + users.size() + " users");

        } catch (Exception e) {
            Log.e(TAG, "Error saving users: " + e.getMessage());
        }
    }

    /**
     * Create default users for initial setup
     */
    private void createDefaultUsers() {
        try {
            // Create default admin
            UserAccount admin = new UserAccount("admin", "admin@fowltyphoid.co.tz", "Msimamizi Mkuu", UserRole.ADMIN);
            admin.phoneNumber = "+255123456789";
            admin.location = "Dar es Salaam";
            admin.permissions = new UserPermissions(UserRole.ADMIN);
            users.add(admin);

            // Create sample veterinarian
            UserAccount vet = new UserAccount("daktari1", "vet1@fowltyphoid.co.tz", "Dr. John Mwalimu", UserRole.VETERINARIAN);
            vet.phoneNumber = "+255987654321";
            vet.location = "Arusha";
            vet.permissions = new UserPermissions(UserRole.VETERINARIAN);
            users.add(vet);

            // Create sample farmer
            UserAccount farmer = new UserAccount("mfugaji1", "farmer1@fowltyphoid.co.tz", "Mama Grace Mwangi", UserRole.FARMER);
            farmer.phoneNumber = "+255765432109";
            farmer.location = "Mwanza";
            farmer.permissions = new UserPermissions(UserRole.FARMER);
            users.add(farmer);

            saveUsers();
            Log.d(TAG, "Created default users");

        } catch (Exception e) {
            Log.e(TAG, "Error creating default users: " + e.getMessage());
        }
    }

    /**
     * Notify listeners of error
     */
    private void notifyError(String error) {
        for (UserManagerListener listener : listeners) {
            listener.onError(error);
        }
    }

    /**
     * Update user statistics
     */
    public void updateUserStatistics(String userId, String statType, int increment) {
        try {
            UserAccount user = getUserById(userId);
            if (user != null) {
                switch (statType) {
                    case "reports":
                        user.statistics.totalReports += increment;
                        break;
                    case "consultations":
                        user.statistics.totalConsultations += increment;
                        break;
                    case "alerts":
                        user.statistics.totalAlertsSent += increment;
                        break;
                    case "logins":
                        user.statistics.loginCount += increment;
                        user.statistics.lastActivity = new Date();
                        break;
                }
                saveUsers();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating user statistics: " + e.getMessage());
        }
    }

    /**
     * Get active users count
     */
    public int getActiveUsersCount() {
        int count = 0;
        for (UserAccount user : users) {
            if (user.status == UserStatus.ACTIVE) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get pending approval users count
     */
    public int getPendingApprovalCount() {
        int count = 0;
        for (UserAccount user : users) {
            if (user.status == UserStatus.PENDING_APPROVAL) {
                count++;
            }
        }
        return count;
    }

    /**
     * Bulk user operations
     */
    public boolean bulkUpdateUserStatus(List<String> userIds, UserStatus newStatus) {
        try {
            for (String userId : userIds) {
                changeUserStatus(userId, newStatus);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in bulk status update: " + e.getMessage());
            return false;
        }
    }

    /**
     * Export users data for backup
     */
    public String exportUsersData() {
        try {
            return gson.toJson(users);
        } catch (Exception e) {
            Log.e(TAG, "Error exporting users data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Import users data from backup
     */
    public boolean importUsersData(String jsonData) {
        try {
            Type listType = new TypeToken<List<UserAccount>>(){}.getType();
            List<UserAccount> importedUsers = gson.fromJson(jsonData, listType);

            if (importedUsers != null) {
                users.clear();
                users.addAll(importedUsers);
                saveUsers();
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error importing users data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user account
     */
    public boolean updateUser(String userId, UserAccount updatedUser) {
        try {
            UserAccount existingUser = getUserById(userId);
            if (existingUser == null) {
                notifyError("Mtumiaji hakupatikana");
                return false;
            }

            // Update user details
            existingUser.fullName = updatedUser.fullName;
            existingUser.email = updatedUser.email;
            existingUser.phoneNumber = updatedUser.phoneNumber;
            existingUser.location = updatedUser.location;
            existingUser.role = updatedUser.role;
            existingUser.status = updatedUser.status;
            existingUser.permissions = updatedUser.permissions;

            saveUsers();

            // Notify listeners
            for (UserManagerListener listener : listeners) {
                listener.onUserUpdated(existingUser);
            }

            Log.d(TAG, "User updated successfully: " + userId);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error updating user: " + e.getMessage());
            notifyError("Kosa katika kusasisha mtumiaji: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete user account
     */
    public boolean deleteUser(String userId) {
        try {
            UserAccount user = getUserById(userId);
            if (user == null) {
                notifyError("Mtumiaji hakupatikana");
                return false;
            }

            users.remove(user);
            saveUsers();

            // Notify listeners
            for (UserManagerListener listener : listeners) {
                listener.onUserDeleted(userId);
            }

            Log.d(TAG, "User deleted successfully: " + userId);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage());
            notifyError("Kosa katika kufuta mtumiaji: " + e.getMessage());
            return false;
        }
    }

    /**
     * Change user status
     */
    public boolean changeUserStatus(String userId, UserStatus newStatus) {
        try {
            UserAccount user = getUserById(userId);
            if (user == null) {
                notifyError("Mtumiaji hakupatikana");
                return false;
            }

            UserStatus oldStatus = user.status;
            user.status = newStatus;
            saveUsers();

            // Notify listeners
            for (UserManagerListener listener : listeners) {
                listener.onUserStatusChanged(userId, newStatus);
            }

            Log.d(TAG, "User status changed from " + oldStatus + " to " + newStatus + " for user: " + userId);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error changing user status: " + e.getMessage());
            notifyError("Kosa katika kubadilisha hali ya mtumiaji: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all users
     */
    public List<UserAccount> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Get users by role
     */
    public List<UserAccount> getUsersByRole(UserRole role) {
        List<UserAccount> roleUsers = new ArrayList<>();
        for (UserAccount user : users) {
            if (user.role == role) {
                roleUsers.add(user);
            }
        }
        return roleUsers;
    }

    /**
     * Get users by status
     */
    public List<UserAccount> getUsersByStatus(UserStatus status) {
        List<UserAccount> statusUsers = new ArrayList<>();
        for (UserAccount user : users) {
            if (user.status == status) {
                statusUsers.add(user);
            }
        }
        return statusUsers;
    }

    /**
     * Get user by ID
     */
    public UserAccount getUserById(String userId) {
        for (UserAccount user : users) {
            if (user.userId.equals(userId)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Get user by username
     */
    public UserAccount getUserByUsername(String username) {
        for (UserAccount user : users) {
            if (user.username.equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Get user by email
     */
    public UserAccount getUserByEmail(String email) {
        for (UserAccount user : users) {
            if (user.email.equals(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Search users by name or username
     */
    public List<UserAccount> searchUsers(String query) {
        List<UserAccount> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (UserAccount user : users) {
            if (user.username.toLowerCase().contains(lowerQuery) ||
                    user.fullName.toLowerCase().contains(lowerQuery) ||
                    user.email.toLowerCase().contains(lowerQuery)) {
                results.add(user);
            }
        }
        return results;
    }

    /**
     * Get user statistics summary
     */
    public UserStatisticsSummary getUserStatisticsSummary() {
        UserStatisticsSummary summary = new UserStatisticsSummary();

        for (UserAccount user : users) {
            switch (user.role) {
                case FARMER:
                    summary.totalFarmers++;
                    break;
                case VETERINARIAN:
                    summary.totalVeterinarians++;
                    break;
                case ADMIN:
                case SUPERVISOR:
                    summary.totalAdmins++;
                    break;
                case FIELD_OFFICER:
                    summary.totalFieldOfficers++;
                    break;
            }

            switch (user.status) {
                case ACTIVE:
                    summary.activeUsers++;
                    break;
                case INACTIVE:
                    summary.inactiveUsers++;
                    break;
                case SUSPENDED:
                    summary.suspendedUsers++;
                    break;
                case PENDING_APPROVAL:
                    summary.pendingUsers++;
                    break;
                case BLOCKED:
                    summary.blockedUsers++;
                    break;
            }
        }

        summary.totalUsers = users.size();
        return summary;
    }

    /**
     * User statistics summary
     */
    public static class UserStatisticsSummary {
        public int totalUsers = 0;
        public int totalFarmers = 0;
        public int totalVeterinarians = 0;
        public int totalAdmins = 0;
        public int totalFieldOfficers = 0;
        public int activeUsers = 0;
        public int inactiveUsers = 0;
        public int suspendedUsers = 0;
        public int pendingUsers = 0;
        public int blockedUsers = 0;
    }

    /**
     * Load users from SharedPreferences
     */
    private void loadUsers() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String usersJson = prefs.getString(KEY_USERS_LIST, "[]");

            Type listType = new TypeToken<List<UserAccount>>(){}.getType();
            List<UserAccount> loadedUsers = gson.fromJson(usersJson, listType);

            if (loadedUsers != null) {
                users.addAll(loadedUsers);
            }

            // Create default admin user if no users exist
            if (users.isEmpty()) {
                createDefaultUsers();
            }

            Log.d(TAG, "Loaded " + users.size() + " users");

        } catch (Exception e) {
            Log.e(TAG, "Error loading users: " + e.getMessage());
            createDefaultUsers();
        }
    }
}