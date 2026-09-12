package com.taskflow.gui;

import com.taskflow.model.Task;
import com.taskflow.service.TaskScheduler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Basic Swing GUI visualizer for TaskFlow application.
 */
public class TaskFlowGUI extends JFrame {

    private final TaskScheduler scheduler;
    private final DefaultTableModel tableModel;
    private final JTextArea logArea;
    private final JTextField searchField;

    public TaskFlowGUI(TaskScheduler scheduler) {
        this.scheduler = scheduler;

        setTitle("TaskFlow - Priority Task Scheduler & DSA Visualizer");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnAdd = new JButton("➕ Add Task");
        JButton btnNext = new JButton("⭐ View Next");
        JButton btnComplete = new JButton("✅ Complete Next");
        JButton btnUndo = new JButton("↩ Undo");
        JButton btnTopo = new JButton("🔗 Execution Order");

        toolBar.add(btnAdd);
        toolBar.add(btnNext);
        toolBar.add(btnComplete);
        toolBar.add(btnUndo);
        toolBar.add(btnTopo);

        searchField = new JTextField(15);
        JButton btnSearch = new JButton("🔍 Search Prefix");
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("Search Prefix: "));
        toolBar.add(searchField);
        toolBar.add(btnSearch);

        add(toolBar, BorderLayout.NORTH);

        // Table Panel (Center)
        String[] columns = {"ID", "Title", "Priority", "Deadline", "Completed", "Tags", "Dependencies"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable taskTable = new JTable(tableModel);
        taskTable.setRowHeight(24);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);

        // Bottom Log Panel
        logArea = new JTextArea(6, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        // Action Listeners
        btnAdd.addActionListener(e -> showAddTaskDialog());
        btnNext.addActionListener(e -> {
            Task next = scheduler.getNextTask();
            if (next != null) {
                log("⭐ Next Highest Priority Task: " + next);
                JOptionPane.showMessageDialog(this, "Next Priority Task:\n" + next.getTitle() + "\nPriority: " + next.getPriority() + "\nDeadline: " + next.getDeadline(), "Highest Priority Task", JOptionPane.INFORMATION_MESSAGE);
            } else {
                log("No pending tasks in MinHeap.");
            }
        });

        btnComplete.addActionListener(e -> {
            try {
                Task completed = scheduler.completeNextTask();
                log("✅ Completed task: " + completed.getTitle() + " (Priority " + completed.getPriority() + ")");
                refreshTable();
            } catch (Exception ex) {
                log("❌ " + ex.getMessage());
            }
        });

        btnUndo.addActionListener(e -> {
            String msg = scheduler.undoLastAction();
            log("↩ " + msg);
            refreshTable();
        });

        btnTopo.addActionListener(e -> {
            try {
                List<Task> order = scheduler.getValidExecutionOrder();
                StringBuilder sb = new StringBuilder("Topological Execution Order (Kahn's Algorithm):\n");
                for (int i = 0; i < order.size(); i++) {
                    sb.append(i + 1).append(". ").append(order.get(i).getTitle()).append(" (ID: ").append(order.get(i).getId()).append(")\n");
                }
                log(sb.toString());
                JOptionPane.showMessageDialog(this, sb.toString(), "Topological Execution Order", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                log("❌ " + ex.getMessage());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cycle Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnSearch.addActionListener(e -> {
            String prefix = searchField.getText();
            List<Task> results = scheduler.searchTasksByPrefix(prefix);
            log("🔍 Autocomplete results for prefix '" + prefix + "': " + results.size() + " task(s) found.");
            updateTable(results);
        });

        refreshTable();
    }

    private void showAddTaskDialog() {
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JComboBox<Integer> priorityCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextField tagsField = new JTextField("work,java");

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Priority (1 = Highest):"));
        panel.add(priorityCombo);
        panel.add(new JLabel("Tags (comma separated):"));
        panel.add(tagsField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText();
            if (title != null && !title.trim().isEmpty()) {
                Task t = new Task(title, descField.getText(), (Integer) priorityCombo.getSelectedItem(), LocalDateTime.now().plusDays(2));
                for (String tag : tagsField.getText().split(",")) {
                    t.addTag(tag);
                }
                scheduler.addTask(t);
                log("➕ Added task: " + title);
                refreshTable();
            }
        }
    }

    private void refreshTable() {
        updateTable(scheduler.getAllTasks());
    }

    private void updateTable(List<Task> tasks) {
        tableModel.setRowCount(0);
        for (Task t : tasks) {
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getTitle(),
                    t.getPriority(),
                    t.getDeadline() != null ? t.getDeadline().toString() : "None",
                    t.isCompleted() ? "Yes" : "No",
                    String.join(", ", t.getTags()),
                    String.join(", ", t.getDependencies())
            });
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
