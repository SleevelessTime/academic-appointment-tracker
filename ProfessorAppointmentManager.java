import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer; // TableCellRenderer için import
import java.awt.*;
import java.awt.event.ActionEvent;       // ActionEvent için import
import java.awt.event.ActionListener;    // ActionListener için import
import java.util.List;

public class ProfessorAppointmentManager extends JFrame {
    private User professor;
    private JTable appointmentsTable;
    private DefaultTableModel tableModel;
    private AppointmentDAO appointmentDAO;
    private NotificationDAO notificationDAO;


    public ProfessorAppointmentManager(User professor) {
        this.professor = professor;
        this.appointmentDAO = new AppointmentDAO();
        this.notificationDAO = new NotificationDAO();

        setTitle("Manage Appointments - Prof. " + professor.getUsername());
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Student", "Date & Time", "Status", "Approve", "Reject"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // Sadece Approve ve Reject sütunları
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 5) return JButton.class;
                return Object.class; // Diğer sütunlar için Object
            }
        };
        appointmentsTable = new JTable(tableModel);

        // Buton sütunları için custom renderer ve editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();

        appointmentsTable.getColumn("Approve").setCellRenderer(buttonRenderer);
        // JTable referansını ButtonEditor'a constructor ile veriyoruz
        appointmentsTable.getColumn("Approve").setCellEditor(new ButtonEditor(new JCheckBox(), appointmentsTable, "Approve", this::approveAppointment));

        appointmentsTable.getColumn("Reject").setCellRenderer(buttonRenderer);
        // JTable referansını ButtonEditor'a constructor ile veriyoruz
        appointmentsTable.getColumn("Reject").setCellEditor(new ButtonEditor(new JCheckBox(), appointmentsTable, "Reject", this::rejectAppointment));


        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> loadAppointments());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadAppointments();
    }

    private void loadAppointments() {
        tableModel.setRowCount(0);
        List<Appointment> appointments = appointmentDAO.getProfessorAppointments(professor.getId());
        for (Appointment apt : appointments) {
            Object[] row = new Object[]{
                    apt.getId(),
                    apt.getStudentName() != null ? apt.getStudentName() : "Student ID: " + apt.getStudentId(),
                    apt.getFormattedAppointmentTime(),
                    apt.getStatus(),
                    "Approve",
                    "Reject"
            };
            tableModel.addRow(row);
        }
    }

    private void approveAppointment(int appointmentId, int row) { // Hangi satırda işlem yapıldığını bilmek için row eklendi
        updateAppointmentStatus(appointmentId, "approved", "Appointment Approved", row);
    }

    private void rejectAppointment(int appointmentId, int row) { // Hangi satırda işlem yapıldığını bilmek için row eklendi
        updateAppointmentStatus(appointmentId, "rejected", "Appointment Rejected", row);
    }

    private void updateAppointmentStatus(int appointmentId, String newStatus, String notificationMessagePrefix, int row) {
        // Satırdaki mevcut durumu kontrol et
        String currentDBStatus = tableModel.getValueAt(row, 3).toString(); // Status sütunu

        if (!"pending".equalsIgnoreCase(currentDBStatus)) {
            JOptionPane.showMessageDialog(this, "This appointment is no longer pending (current status: " + currentDBStatus + "). Action aborted.", "Action Denied", JOptionPane.WARNING_MESSAGE);
            loadAppointments(); // Tabloyu tazeleyerek tutarsızlığı gider
            return;
        }

        Appointment targetAppointment = null;
        // Optimizasyon: Direkt ID ile DAO'dan çekmek yerine, eğer tablo güncelse buradan bilgi alabiliriz.
        // Ancak en sağlıklısı DAO'dan çekmek olabilir, ama çok sayıda randevu varsa performans etkileyebilir.
        // Şimdilik basit tutalım ve öğrenci ID'si için DAO kullanalım.
        List<Appointment> currentAppointments = appointmentDAO.getProfessorAppointments(professor.getId()); // Veritabanından en güncel listeyi al
        for (Appointment apt : currentAppointments) {
            if (apt.getId() == appointmentId) {
                targetAppointment = apt;
                break;
            }
        }

        if (targetAppointment == null) {
            JOptionPane.showMessageDialog(this, "Appointment with ID: "+ appointmentId +" not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
            loadAppointments();
            return;
        }
        // Tekrar DB'den gelen status'u kontrol et, UI ile DB arasında tutarsızlık olmaması için
        if (!"pending".equalsIgnoreCase(targetAppointment.getStatus())) {
            JOptionPane.showMessageDialog(this, "This appointment is no longer pending (DB status: " + targetAppointment.getStatus() + "). Action aborted.", "Action Denied", JOptionPane.WARNING_MESSAGE);
            loadAppointments();
            return;
        }

        appointmentDAO.updateAppointmentStatus(appointmentId, newStatus);
        notificationDAO.addNotification(targetAppointment.getStudentId(),
                notificationMessagePrefix + " by Prof. " + professor.getUsername() +
                        " for " + targetAppointment.getFormattedAppointmentTime());
        JOptionPane.showMessageDialog(this, notificationMessagePrefix + " (ID: " + appointmentId + ")", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        loadAppointments();
    }


    // Inner classes for JTable button functionality
    interface TableButtonAction {
        void execute(int id, int row); // row parametresi eklendi
    }

    // ButtonRenderer sınıfı, JTable referansına ihtiyaç duymaz, olduğu gibi kalabilir.
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            String status = (String) table.getModel().getValueAt(row, 3);
            setEnabled("pending".equalsIgnoreCase(status)); // Durum "pending" ise butonu aktif et
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private TableButtonAction action;
        private int currentId;
        private int currentRow; // Düzenlenen satırın indeksi
        private JTable tableInstance; // JTable referansı

        public ButtonEditor(JCheckBox checkBox, JTable table, String text, TableButtonAction action) {
            super(checkBox);
            this.tableInstance = table; // JTable referansını al
            this.action = action;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() { // Lambda yerine ActionListener
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row; // Mevcut satırı sakla
            label = (value == null) ? "" : value.toString();
            button.setText(label);

            Object idValue = tableInstance.getModel().getValueAt(row, 0); // ID'yi modelden al
            if (idValue instanceof Integer) {
                currentId = (Integer) idValue;
            } else {
                // Hata durumu veya ID'nin farklı bir türde olması
                currentId = -1; // veya uygun bir hata işleme
                System.err.println("ButtonEditor: ID is not an Integer at row " + row);
            }

            String status = (String) tableInstance.getModel().getValueAt(row, 3);
            button.setEnabled("pending".equalsIgnoreCase(status));

            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed && currentId != -1 && currentRow != -1) { // currentRow kontrolü eklendi
                // JTable referansını doğrudan kullan
                if (tableInstance != null && tableInstance.getModel() != null) {
                    // getSelectedRow() yerine currentRow (editlenen satır) kullanılmalı
                    String status = (String) tableInstance.getModel().getValueAt(currentRow, 3); // Status sütunu
                    if ("pending".equalsIgnoreCase(status)) {
                        action.execute(currentId, currentRow); // action'a currentRow'u da gönder
                    }
                } else {
                    System.err.println("ButtonEditor.getCellEditorValue: tableInstance or its model is null.");
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        // Bu metod genellikle ActionListener içinde fireEditingStopped çağrıldığında otomatik olarak çağrılır.
        // Ancak, bazı durumlarda manuel olarak çağrılması gerekebilir veya gereksiz olabilir.
        // Şimdilik varsayılan davranışı kullanıyoruz.
    }
}