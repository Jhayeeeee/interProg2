import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static final String DATA_DIR = "data/";
    private static final String USERS_FILE = DATA_DIR + "users.txt";
    private static final String APPOINTMENTS_FILE = DATA_DIR + "appointments.txt";
    private static final String VALID_STUDENTS_FILE = DATA_DIR + "valid_students.txt";
    private static final String CLINIC_SCHEDULE_FILE = DATA_DIR + "clinic_schedule.txt";
    private static final String SETTINGS_FILE = DATA_DIR + "settings.txt";

    private static String validDomain = "@plm.edu.ph";

    static {
        initDataFiles();
    }

    private static void initDataFiles() {
        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) dir.mkdirs();

            ensureFileExists(USERS_FILE);
            ensureFileExists(APPOINTMENTS_FILE);
            ensureFileExists(VALID_STUDENTS_FILE);
            ensureFileExists(CLINIC_SCHEDULE_FILE);
            ensureFileExists(SETTINGS_FILE);

            // Load settings
            loadSettings();

            // Auto-complete past pending appointments
            autoCompletePastAppointments();

            // Seed valid students if empty
            if (new File(VALID_STUDENTS_FILE).length() == 0) {
                saveToFile(VALID_STUDENTS_FILE, "2024-0001|John Doe|john@plm.edu.ph\n2024-0002|Jane Smith|jane@plm.edu.ph\n2024-0003|Bob Johnson|bob@plm.edu.ph\nADMIN-001|Admin User|admin@univ.edu", true);
            }

            // Seed Admin if empty
            if (new File(USERS_FILE).length() == 0) {
                saveToFile(USERS_FILE, "ADMIN-001|Clinic Admin|admin123|ADMIN", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadSettings() {
        try (BufferedReader br = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            String line = br.readLine();
            if (line != null && line.contains("@")) validDomain = line.trim();
        } catch (IOException e) {}
    }

    public static String getValidDomain() { return validDomain; }
    public static void setValidDomain(String domain) {
        validDomain = domain;
        saveToFile(SETTINGS_FILE, domain, false);
    }

    private static void autoCompletePastAppointments() {
        List<String> lines = new ArrayList<>();
        boolean changed = false;
        LocalDate today = LocalDate.now();
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6 && "Pending".equalsIgnoreCase(parts[5].trim())) {
                    try {
                        LocalDate apptDate = LocalDate.parse(parts[3]);
                        if (apptDate.isBefore(today)) {
                            parts[5] = "Done"; // Mark as Done if date passed
                            // id|studentID|studentName|date|concern|status|dentistName|preCheck
                            if (parts.length < 7) {
                                String[] newParts = new String[8];
                                System.arraycopy(parts, 0, newParts, 0, parts.length);
                                for(int i=parts.length; i<8; i++) newParts[i] = "N/A";
                                parts = newParts;
                            }
                            parts[6] = "Expired/Done"; 
                            line = String.join("|", parts);
                            changed = true;
                        }
                    } catch (Exception e) {}
                }
                lines.add(line);
            }
        } catch (IOException e) { e.printStackTrace(); }

        if (changed) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
                for (String l : lines) pw.println(l);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private static void ensureFileExists(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) f.createNewFile();
    }

    private static void saveToFile(String path, String content, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, append))) {
            bw.write(content);
            if (!content.endsWith("\n")) bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidStudentCredentials(String studentID, String email) {
        try (BufferedReader br = new BufferedReader(new FileReader(VALID_STUDENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    if (parts[0].trim().equals(studentID.trim()) && parts[2].trim().equalsIgnoreCase(email.trim())) {
                        return true;
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }

    public static List<String[]> getAllValidStudents() {
        List<String[]> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(VALID_STUDENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) list.add(parts);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    public static void deleteUser(String studentID) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 1 && !parts[0].equals(studentID)) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {}

        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) {}
    }

    public static boolean updateUserPassword(String studentID, String currentPass, String newPass) {
        List<String> lines = new ArrayList<>();
        boolean success = false;
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3 && parts[0].equals(studentID) && parts[2].equals(currentPass)) {
                    parts[2] = newPass; // Update password
                    lines.add(String.join("|", parts));
                    success = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {}

        if (success) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
                for (String l : lines) pw.println(l);
            } catch (IOException e) {}
        }
        return success;
    }

    public static boolean updateUserNameAndPassword(String studentID, String newName, String currentPass, String newPass) {
        List<String> lines = new ArrayList<>();
        boolean success = false;
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4 && parts[0].equals(studentID) && parts[2].equals(currentPass)) {
                    parts[1] = newName;
                    if (newPass != null && !newPass.isEmpty()) parts[2] = newPass;
                    lines.add(String.join("|", parts));
                    success = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {}

        if (success) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
                for (String l : lines) pw.println(l);
            } catch (IOException e) {}
        }
        return success;
    }

    public static void registerValidStudent(String id, String name, String email) {
        saveToFile(VALID_STUDENTS_FILE, id + "|" + name + "|" + email, true);
    }

    public static void removeValidStudent(String id) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(VALID_STUDENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(id + "|")) lines.add(line);
            }
        } catch (IOException e) { e.printStackTrace(); }
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(VALID_STUDENTS_FILE))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- Clinic Schedule Management ---
    public static void setClinicAvailability(String date, String start, String end, String doctor, int slotLimit) {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(CLINIC_SCHEDULE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts[0].equals(date)) {
                    lines.add(date + "|" + start + "|" + end + "|" + doctor + "|" + slotLimit);
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }

        if (!found) lines.add(date + "|" + start + "|" + end + "|" + doctor + "|" + slotLimit);

        try (PrintWriter pw = new PrintWriter(new FileWriter(CLINIC_SCHEDULE_FILE))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void setClinicClosed(String date) {
        setClinicAvailability(date, "CLOSED", "CLOSED", "N/A", 0);
    }

    public static String[] getClinicSchedule(String date) {
        try (BufferedReader br = new BufferedReader(new FileReader(CLINIC_SCHEDULE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts[0].equals(date)) return parts;
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public static List<String> getAvailableDates() {
        List<String> dates = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CLINIC_SCHEDULE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                dates.add(parts[0]);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return dates;
    }

    public static boolean registerUser(String studentID, String fullName, String password) {
        // Check if student_id already exists in USERS
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(studentID + "|")) return false;
            }
        } catch (IOException e) {}

        String data = String.join("|", studentID, fullName, password, "STUDENT");
        saveToFile(USERS_FILE, data, true);
        return true;
    }

    public static User validateUser(String studentID, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4 && parts[0].equalsIgnoreCase(studentID) && parts[2].equals(password)) {
                    User u = new User();
                    u.studentID = parts[0];
                    u.fullName = parts[1];
                    u.password = parts[2];
                    u.role = parts[3];
                    u.name = u.fullName;
                    return u;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean hasPendingAppointment(String studentID) {
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6 && parts[1].trim().equals(studentID.trim()) && "Pending".equalsIgnoreCase(parts[5].trim())) {
                    return true;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }


    public static boolean addAppointment(String studentID, String name, String date, String concern, String preCheck) {
        // Check slots
        String[] sched = getClinicSchedule(date);
        if (sched != null && sched.length >= 5) {
            int limit = Integer.parseInt(sched[4]);
            if (getAppointmentCountForDate(date) >= limit) return false; // Full
        }

        int id = (int) (System.currentTimeMillis() % 1000000);
        // id|studentID|studentName|date|concern|status|dentistName|preCheck
        String data = String.join("|", String.valueOf(id), studentID, name, date, concern, "Pending", "N/A", preCheck);
        saveToFile(APPOINTMENTS_FILE, data, true);
        return true;
    }

    public static int getAppointmentCountForDate(String date) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4 && parts[3].equals(date)) count++;
            }
        } catch (IOException e) {}
        return count;
    }

    public static void deleteAppointment(int id) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(id + "|")) lines.add(line);
            }
        } catch (IOException e) {}
        try (PrintWriter pw = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) {}
    }

    public static void updateAppointmentData(int id, String concern, String preCheck) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 8 && Integer.parseInt(parts[0]) == id) {
                    parts[4] = concern;
                    parts[7] = preCheck;
                    lines.add(String.join("|", parts));
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {}
        try (PrintWriter pw = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) {}
    }

    public static void updateAppointmentStatus(int id, String newStatus) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6 && Integer.parseInt(parts[0]) == id) {
                    parts[5] = newStatus;
                    lines.add(String.join("|", parts));
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void updateAppointmentStatusWithDentist(int id, String newStatus, String dentistName) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6 && Integer.parseInt(parts[0]) == id) {
                    // id|studentID|studentName|date|concern|status|dentistName|preCheck
                    String preCheck = parts.length >= 8 ? parts[7] : "N/A";
                    String base = String.join("|", parts[0], parts[1], parts[2], parts[3], parts[4], newStatus, dentistName, preCheck);
                    lines.add(base);
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static List<Appointment> getAppointmentsByDate(String date) {
        List<Appointment> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6 && parts[3].equals(date)) {
                    Appointment a = new Appointment();
                    a.id = Integer.parseInt(parts[0]);
                    a.studentID = parts[1];
                    a.studentName = parts[2];
                    a.date = parts[3];
                    a.concern = parts[4];
                    a.status = parts[5];
                    if (parts.length >= 7) a.dentistName = parts[6];
                    if (parts.length >= 8) a.preCheckData = parts[7];
                    list.add(a);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Appointment> getStudentAppointments(String studentID) {
        List<Appointment> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6 && parts[1].equals(studentID)) {
                    Appointment a = new Appointment();
                    a.id = Integer.parseInt(parts[0]);
                    a.studentID = parts[1];
                    a.studentName = parts[2];
                    a.date = parts[3];
                    a.concern = parts[4];
                    a.status = parts[5];
                    if (parts.length >= 7) a.dentistName = parts[6];
                    if (parts.length >= 8) a.preCheckData = parts[7];
                    list.add(a);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Appointment> getAllCompletedAppointments() {
        List<Appointment> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6 && "Done".equalsIgnoreCase(parts[5])) {
                    Appointment a = new Appointment();
                    a.id = Integer.parseInt(parts[0]);
                    a.studentID = parts[1];
                    a.studentName = parts[2];
                    a.date = parts[3];
                    a.concern = parts[4];
                    a.status = parts[5];
                    if (parts.length >= 7) a.dentistName = parts[6];
                    if (parts.length >= 8) a.preCheckData = parts[7];
                    list.add(a);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    public static class User {
        public int id;
        public String studentID;
        public String fullName;
        public String password; 
        public String name;
        public String role;
    }

    public static class Appointment {
        public int id;
        public String studentID;
        public String studentName;
        public String date;
        public String concern;
        public String status;
        public String dentistName;
        public String preCheckData;
    }
}
