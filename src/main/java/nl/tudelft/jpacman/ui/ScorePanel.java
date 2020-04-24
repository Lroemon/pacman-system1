package nl.tudelft.jpacman.ui;

import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Player;

/**
 * A panel consisting of a column for each player, with the numbered players on
 * top and their respective scores underneath.
 *
 * @author Jeroen Roosen 
 *
 */
public class ScorePanel extends JPanel {

    /**
     * Default serialisation ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The index of the label which displays the player score.
     */
    private static final int PLAYER_SCORE_LABEL_INDEX = 0;

    /**
     * The index of the label which displays the current player life number.
     */
    private static final int PLAYER_LIFE_LEFT_LABEL_INDEX = 1;

    /**
     * The map of players and the labels their scores are on.
     */
    private final Map<Player, JLabel[]> scoreLabels;

    /**
     * The default way in which the score is shown.
     */
    public static final ScoreFormatter DEFAULT_SCORE_FORMATTER =
        (Player player) -> String.format("Score: %3d", player.getScore());

    /**
     * The way to format the score information.
     */
    private ScoreFormatter scoreFormatter = DEFAULT_SCORE_FORMATTER;

    /**
     * Creates a new score panel with a column for each player.
     *
     * @param players
     *            The players to display the scores of.
     */
    public ScorePanel(List<Player> players) {
        super();
        assert players != null;

        setLayout(new GridLayout(3, players.size()));

        for (int i = 1; i <= players.size(); i++) {
            add(new JLabel("Player " + i, JLabel.CENTER));
        }
        scoreLabels = new LinkedHashMap<>();
        for (Player player : players) {
            JLabel scoreLabel = new JLabel("0", JLabel.CENTER);
            JLabel lifeLeftLabel = new JLabel(getLifeLeftLabelText(player), JLabel.CENTER);

            scoreLabels.put(player, new JLabel[]{scoreLabel, lifeLeftLabel});

            add(scoreLabel);
            add(lifeLeftLabel);
        }
    }

    private String getLifeLeftLabelText(Player player){
        return "Life(s) : " + player.getLifeLeft();
    }

    /**
     * Refreshes the scores of the players.
     */
    protected void refresh() {
        for (Map.Entry<Player, JLabel[]> entry : scoreLabels.entrySet()) {
            Player player = entry.getKey();
            JLabel[] labels = entry.getValue();

            String score = "";
            if (!player.isAlive()) {
                score = "You died. ";
            }
            score += scoreFormatter.format(player);
            labels[PLAYER_SCORE_LABEL_INDEX].setText(score);
            labels[PLAYER_LIFE_LEFT_LABEL_INDEX].setText(getLifeLeftLabelText(player));
        }
    }

    /**
     * Provide means to format the score for a given player.
     */
    public interface ScoreFormatter {

        /**
         * Format the score of a given player.
         * @param player The player and its score
         * @return Formatted score.
         */
        String format(Player player);
    }

    /**
     * Let the score panel use a dedicated score formatter.
     * @param scoreFormatter Score formatter to be used.
     */
    public void setScoreFormatter(ScoreFormatter scoreFormatter) {
        assert scoreFormatter != null;
        this.scoreFormatter = scoreFormatter;
    }
}
