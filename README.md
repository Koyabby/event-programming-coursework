# event-programming-coursework
Event Programming coursework, Weeks 1–10

## Week 10: University Management System

The standalone JavaFX Maven project is in `week10-university-system`.

1. Start MySQL and execute `week10-university-system/src/main/resources/sql/university_db.sql` in MySQL Workbench. The script creates the database and tables without deleting existing data.
2. Create a private local configuration file outside the repository. In PowerShell, run these commands from the repository root, then enter the MySQL username and password in Notepad:

   ```powershell
   New-Item -ItemType Directory -Force "$HOME\.university-system" | Out-Null
   Copy-Item "week10-university-system\database.properties.example" "$HOME\.university-system\database.properties"
   notepad "$HOME\.university-system\database.properties"
   ```

3. Compile and test from VS Code's terminal:

   ```powershell
   cd week10-university-system
   mvn clean test
   ```

   Live MySQL integration tests run when database credentials are configured; otherwise they are skipped and the model/validation tests still run.

4. Launch the application:

   ```powershell
   mvn javafx:run
   ```

For the lecturer demonstration, open **Students**, enter a registration number, first name and last name, and click **Save Student**. The table refreshes from MySQL, the Save button is bound to the form state, and the console prints a heartbeat every five seconds. The Dashboard also refreshes its database student count every five seconds.
