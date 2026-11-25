import javax.swing.*;
import java.awt.*;

public class RegistrationForm extends JFrame {
    private final JTextField nameField = new JTextField(30);
    private final JTextField emailField = new JTextField(30);
    private final JPasswordField passwordField = new JPasswordField(30);
    private final JRadioButton maleRadio = new JRadioButton("Male");
    private final JRadioButton femaleRadio = new JRadioButton("Female");
    private final JRadioButton otherRadio = new JRadioButton("Other");
    private final ButtonGroup genderGroup = new ButtonGroup();
    private final JCheckBox sportsCheck = new JCheckBox("Sports");
    private final JCheckBox musicCheck = new JCheckBox("Music");
    private final JCheckBox readingCheck = new JCheckBox("Reading");
    private final JComboBox<String> countryBox = new JComboBox<>(new String[]{"Kenya", "Tanzania", "Uganda", "Rwanda"});
    private final JTextArea commentsArea = new JTextArea(3, 30);
    private final JLabel statusLabel = new JLabel("Status: Ready");

    public RegistrationForm() {
        super("Registration Form");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel titleLabel = new JLabel("Registration Form", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        form.add(titleLabel, c);
        c.gridwidth = 1;
        addRow(form, c, 1, "Full Name:", nameField);
        addRow(form, c, 2, "Email:", emailField);
        addRow(form, c, 3, "Password:", passwordField);
        c.gridx = 0; c.gridy = 4; form.add(new JLabel("Gender:"), c);
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        genderGroup.add(maleRadio); genderGroup.add(femaleRadio); genderGroup.add(otherRadio);
        genderPanel.add(maleRadio); genderPanel.add(femaleRadio); genderPanel.add(otherRadio);
        c.gridx = 1; form.add(genderPanel, c);
        c.gridx = 0; c.gridy = 5; form.add(new JLabel("Interests:"), c);
        JPanel interests = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        interests.add(sportsCheck); interests.add(musicCheck); interests.add(readingCheck);
        c.gridx = 1; form.add(interests, c);
        addRow(form, c, 6, "Country:", countryBox);
        c.gridx = 0; c.gridy = 7; form.add(new JLabel("Comments:"), c);
        c.gridx = 1; form.add(new JScrollPane(commentsArea), c);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton submitButton = new JButton("Submit"); submitButton.setBackground(Color.GREEN);
        JButton clearButton = new JButton("Clear");
        JButton exitButton = new JButton("Exit"); exitButton.setBackground(Color.RED);
        buttons.add(submitButton); buttons.add(clearButton); buttons.add(exitButton);
        c.gridx = 0; c.gridy = 8; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER; form.add(buttons, c);
        c.gridy = 9; c.anchor = GridBagConstraints.WEST; form.add(statusLabel, c);
        add(form);
        submitButton.addActionListener(this::submitButtonActionPerformed);
        clearButton.addActionListener(this::clearButtonActionPerformed);
        exitButton.addActionListener(this::exitButtonActionPerformed);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component field) {
        c.gridx = 0; c.gridy = row; panel.add(new JLabel(label), c);
        c.gridx = 1; panel.add(field, c);
    }

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (name.isEmpty()) { statusLabel.setText("Error: Please enter your name"); statusLabel.setForeground(Color.RED); return; }
        if (email.isEmpty() || !email.contains("@")) { statusLabel.setText("Error: Please enter a valid email"); statusLabel.setForeground(Color.RED); return; }
        if (password.length() < 6) { statusLabel.setText("Error: Password must be at least 6 characters"); statusLabel.setForeground(Color.RED); return; }
        String gender = "Not specified";
        if (maleRadio.isSelected()) gender = "Male"; else if (femaleRadio.isSelected()) gender = "Female"; else if (otherRadio.isSelected()) gender = "Other";
        StringBuilder interests = new StringBuilder();
        if (sportsCheck.isSelected()) interests.append("Sports ");
        if (musicCheck.isSelected()) interests.append("Music ");
        if (readingCheck.isSelected()) interests.append("Reading ");
        String country = (String) countryBox.getSelectedItem();
        String comments = commentsArea.getText().trim();
        String message = "Registration Successful!\n\n" + "Name: " + name + "\n" + "Email: " + email + "\n" + "Gender: " + gender + "\n" + "Interests: " + (interests.length() > 0 ? interests.toString() : "None") + "\n" + "Country: " + country + "\n" + "Comments: " + (comments.isEmpty() ? "None" : comments);
        JOptionPane.showMessageDialog(this, message, "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText("Registration submitted successfully!");
        statusLabel.setForeground(new Color(0, 128, 0));
    }

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
        nameField.setText(""); emailField.setText(""); passwordField.setText(""); genderGroup.clearSelection();
        sportsCheck.setSelected(false); musicCheck.setSelected(false); readingCheck.setSelected(false);
        countryBox.setSelectedIndex(0); commentsArea.setText("");
        statusLabel.setText("Status: Form cleared"); statusLabel.setForeground(Color.BLACK);
    }

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationForm().setVisible(true));
    }
}