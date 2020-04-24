package nl.tudelft.jpacman.ui;

import nl.tudelft.jpacman.level.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LevelStatsPanel extends JPanel {

    /**
     * Default serialisation ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * the format of the timer.
     */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("mm:ss.SSS");

    /**
     * The timer.
     */
    private Timer timer;

    /**
     * The label with the timer of this game.
     */
    private JLabel timeLabel;

    /**
     * The level to display information.
     */
    private Level level;

    /**
     *
     * @param level the level to display Information.
     */
    public LevelStatsPanel(Level level){
        this.level = level;

        timeLabel = new JLabel("INIT", JLabel.CENTER);

        setLayout(new BorderLayout());
        add(timeLabel, BorderLayout.CENTER);

        this.timer =  new Timer(5, new ActionListener(){
            private long count;
            @Override
            public void actionPerformed(ActionEvent e) {
                if(level.isInProgress() && !level.areGhostsScared()){
                    count += 5;
                    timeLabel.setText(getLevelTime(count));
                }
            }
        });

        this.timer.start();
    }

    /**
     *
     * @return a string representing the time of the game.
     */
    private String getLevelTime(long timeInMilli){
        Date date = new Date(timeInMilli);
        return "Time : " + SDF.format(date);
    }
}
