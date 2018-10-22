package wumpusworld;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * GUI for the Wumpus World. Only supports worlds of 
 * size 4.
 * 
 * @author Johan Hagelbäck
 */
public class GUI implements ActionListener
{
    private JFrame frame;
    private JPanel gamepanel;
    private JLabel score;
    private JLabel status;
    private JLabel mapListLabel;
    private JTextField qtableCountField;
    private JTextField stateField;
    private JTextField[] stateQValueFields;
    private JTextField stateBestAction;
    private JTextField trainEpisodesField;
    private World w;
    private WumpusEnv env;
    private Agent agent;
    private JPanel[][] blocks;
    private JComboBox mapList;
    private Vector<WorldMap> maps;
    
    private ImageIcon l_breeze;
    private ImageIcon l_stench;
    private ImageIcon l_pit;
    private ImageIcon l_glitter;
    private ImageIcon l_wumpus;
    private ImageIcon l_player_up;
    private ImageIcon l_player_down;
    private ImageIcon l_player_left;
    private ImageIcon l_player_right;
    
    /**
     * Creates and start the GUI.
     */
    public GUI()
    {
        if (!checkResources())
        {
            JOptionPane.showMessageDialog(null, "Unable to start GUI. Missing icons.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        MapReader mr = new MapReader();
        maps = mr.readMaps();
        if (maps.size() > 0)
        {
            w = maps.get(0).generateWorld();
        }
        else
        {
            w = MapGenerator.getRandomMap((int)System.currentTimeMillis()).generateWorld();
        }
        
        l_breeze = new ImageIcon("gfx/B.png");
        l_stench = new ImageIcon("gfx/S.png");
        l_pit = new ImageIcon("gfx/P.png");
        l_glitter = new ImageIcon("gfx/G.png");
        l_wumpus = new ImageIcon("gfx/W.png");
        l_player_up = new ImageIcon("gfx/PU.png");
        l_player_down = new ImageIcon("gfx/PD.png");
        l_player_left = new ImageIcon("gfx/PL.png");
        l_player_right = new ImageIcon("gfx/PR.png");
        
        env = new WumpusEnv();
        
        createWindow();
    }
    
    /**
     * Checks if all resources (icons) are found.
     * 
     * @return True if all resources are found, false otherwise. 
     */
    private boolean checkResources()
    {
        try
        {
            File f;
            f = new File("gfx/B.png");
            if (!f.exists()) return false;
            f = new File("gfx/S.png");
            if (!f.exists()) return false;
            f = new File("gfx/P.png");
            if (!f.exists()) return false;
            f = new File("gfx/G.png");
            if (!f.exists()) return false;
            f = new File("gfx/W.png");
            if (!f.exists()) return false;
            f = new File("gfx/PU.png");
            if (!f.exists()) return false;
            f = new File("gfx/PD.png");
            if (!f.exists()) return false;
            f = new File("gfx/PL.png");
            if (!f.exists()) return false;
            f = new File("gfx/PR.png");
            if (!f.exists()) return false;
        }
        catch (Exception ex)
        {
            return false;
        }
        return true;
    }
    
    /**
     * Creates all window components.
     */
    private void createWindow()
    {
        frame = new JFrame("Wumpus World");
        frame.setSize(1400, 660);
        frame.getContentPane().setLayout(new GridBagLayout());        
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        GridBagConstraints frameGBC = new GridBagConstraints();
        frameGBC.insets = new Insets(4, 4, 4, 4);
        frameGBC.ipadx = 10;
        frameGBC.weightx = 0.0; 
        frameGBC.weighty = 0.0;
        frameGBC.fill = GridBagConstraints.HORIZONTAL;
        frameGBC.anchor = GridBagConstraints.NORTHWEST;
       
        // Game Panel creation
        initGamePanel();
        frameGBC.gridx = 0; frameGBC.gridy = 0;
        frameGBC.gridheight = 2;
        frameGBC.weightx = 0.0;
        frame.getContentPane().add(gamepanel, frameGBC);
        frameGBC.gridheight = 1;
         
        // Game Controls UI creation 
        JPanel gameUI = initGameControlsUI();
        frameGBC.gridx = 1; frameGBC.gridy = 0;
        frameGBC.weightx = 0.0;
        frame.getContentPane().add(gameUI, frameGBC);
        
        // Q stats UI creation
        JPanel qstatsUI = initQStatsUI();
        frameGBC.gridx = 1; frameGBC.gridy = 1;
        frameGBC.weightx = 0.0;
        frame.getContentPane().add(qstatsUI, frameGBC);
        
        // Training UI creation
        JPanel trainingUI = initTrainingUI();
        frameGBC.gridx = 2; frameGBC.gridy = 0;
        frameGBC.weightx = 1.0;
        frame.getContentPane().add(trainingUI, frameGBC);
        
        updateGame();
        refreshStats();
        
        //Show window
        frame.setVisible(true);
    }
    
    private void initGamePanel() 
    {
        gamepanel = new JPanel();
        gamepanel.setMinimumSize(new Dimension(600,600));
        gamepanel.setBackground(Color.GRAY);
        gamepanel.setLayout(new GridLayout(4,4));
        
        //Add blocks
        blocks = new JPanel[4][4];
        for (int j = 3; j >= 0; j--)
        {
            for (int i = 0; i < 4; i++)
            {
                blocks[i][j] = new JPanel();
                blocks[i][j].setBackground(Color.white);
                blocks[i][j].setPreferredSize(new Dimension(150,150));
                blocks[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                blocks[i][j].setLayout(new GridLayout(2,2));
                gamepanel.add(blocks[i][j]);
            }
        }
    }
    
    private JPanel initGameControlsUI() 
    {               
        //Add buttons panel
        JPanel gameControlPanel = new JPanel();
        gameControlPanel.setBackground(Color.LIGHT_GRAY);
        gameControlPanel.setLayout(new GridBagLayout());
        // Set GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 1, 5, 1);   
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // === Button Labels ===
        // Label - Status
        status = new JLabel("", SwingConstants.CENTER);
        status.setPreferredSize(new Dimension(150,25));
        gbc.gridx = 0; gbc.gridy = 0;
        gameControlPanel.add(status, gbc);
        
        // Label - Score
        score = new JLabel("Score: 0", SwingConstants.CENTER);
        score.setPreferredSize(new Dimension(150,25));
        gbc.gridx = 1; gbc.gridy = 0;
        gameControlPanel.add(score, gbc);
        
        // Label - Map List
        mapListLabel = new JLabel("Map list: ", SwingConstants.CENTER);
        mapListLabel.setPreferredSize(new Dimension(150,25));
        gbc.gridx = 0; gbc.gridy = 4;
        gameControlPanel.add(mapListLabel, gbc);          
        
        // === Buttons ===
        // Button - Turn Left
        JButton bl = new JButton(new ImageIcon("gfx/TL.png"));
        bl.setActionCommand("TL");
        bl.addActionListener(this);        
        gbc.gridx = 0; gbc.gridy = 1;
        gameControlPanel.add(bl, gbc);
        
        // Button - Move Forward
        JButton bf = new JButton(new ImageIcon("gfx/MF.png"));
        bf.setActionCommand("MF");
        bf.addActionListener(this);
        gbc.gridx = 1; gbc.gridy = 1;
        gameControlPanel.add(bf, gbc);
        
        // Button - Turn Right
        JButton br = new JButton(new ImageIcon("gfx/TR.png"));
        br.setActionCommand("TR");
        br.addActionListener(this);
        gbc.gridx = 2; gbc.gridy = 1;
        gameControlPanel.add(br, gbc);
        
        // Button - Grab
        JButton bg = new JButton("Grab");
        bg.setPreferredSize(new Dimension(150,22));
        bg.setActionCommand("GRAB");
        bg.addActionListener(this);
        gbc.gridx = 0; gbc.gridy = 2;
        gameControlPanel.add(bg, gbc);
        
        // Button - Climb
        JButton bc = new JButton("Climb");
        bc.setPreferredSize(new Dimension(150,22));
        bc.setActionCommand("CLIMB");
        bc.addActionListener(this);
        gbc.gridx = 1; gbc.gridy = 2;
        gameControlPanel.add(bc, gbc);
        
        // Button - Shoot
        JButton bs = new JButton("Shoot");
        bs.setPreferredSize(new Dimension(150,22));
        bs.setActionCommand("SHOOT");
        bs.addActionListener(this);
        gbc.gridx = 2; gbc.gridy = 2;
        gameControlPanel.add(bs, gbc);
        
        // Button - Run Solving Agent
        JButton ba = new JButton("Run Solving Agent");
        ba.setActionCommand("AGENT");
        ba.addActionListener(this);
        gbc.gridx = 1; gbc.gridy = 3;
        gameControlPanel.add(ba, gbc);      

        // Dropdown-List - Map List
        Vector<String> items = new Vector<>();
        for (int i = 0; i < maps.size(); i++)
        {
            items.add((i+1) + "");
        }
        items.add("Random");
        mapList = new JComboBox(items);
        mapList.setPreferredSize(new Dimension(150,25));
        gbc.gridx = 1; gbc.gridy = 4;
        gameControlPanel.add(mapList, gbc);
        
        // Button - New Game
        JButton bn = new JButton("New Game");
        bn.setActionCommand("NEW");
        bn.addActionListener(this);
        gbc.gridx = 2; gbc.gridy = 4;
        gameControlPanel.add(bn, gbc);        
        
        return gameControlPanel;
    }
    
    private JPanel initQStatsUI() 
    {
        // Create Q Stats UI panel
        JPanel qstatsPanel = new JPanel();
        qstatsPanel.setBackground(Color.LIGHT_GRAY);
        qstatsPanel.setLayout(new GridBagLayout());
        
        // Set GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 1, 5, 1);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Create border title
        TitledBorder border = new TitledBorder(new LineBorder(Color.BLACK), "Q stats");
        border.setTitleJustification(TitledBorder.LEFT);
        border.setTitlePosition(TitledBorder.TOP);
        qstatsPanel.setBorder(border);
        
        // === QTable info ===
        // Label - QTable entry count
        JLabel labelQStateCount = new JLabel("QState count: ", SwingConstants.LEFT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        qstatsPanel.add(labelQStateCount, gbc);
        
        // TextBox - QTable entry count
        qtableCountField = new JTextField();
        qtableCountField.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        qstatsPanel.add(qtableCountField, gbc);
        
        // === Current Q State ===
        JPanel currentStatePanel = new JPanel();
        currentStatePanel.setLayout(new GridBagLayout());
        currentStatePanel.setBackground(Color.LIGHT_GRAY);
        TitledBorder currentStateBorder = new TitledBorder(new LineBorder(Color.BLACK), "Current State");
        currentStateBorder.setTitleJustification(TitledBorder.LEFT);
        currentStateBorder.setTitlePosition(TitledBorder.TOP);
        currentStatePanel.setBorder(currentStateBorder);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 1, 1, 1);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.gridwidth = 1;
        gbc2.gridheight = 1;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        
        // Labels for QValues of all actions from current state, as well as best action
        gbc2.gridx = 0; gbc2.weightx = 0.0; gbc2.weighty = 0.0;
        JLabel labelState = new JLabel("State: ", SwingConstants.LEFT);
        gbc2.gridy = 0; currentStatePanel.add(labelState, gbc2);
        JLabel labelQValAction0 = new JLabel("[0] Action - TL: ", SwingConstants.LEFT);
        gbc2.gridy = 1; currentStatePanel.add(labelQValAction0, gbc2);
        JLabel labelQValAction1 = new JLabel("[1] Action - FW: ", SwingConstants.LEFT);
        gbc2.gridy = 2; currentStatePanel.add(labelQValAction1, gbc2);
        JLabel labelQValAction2 = new JLabel("[2] Action - TR: ", SwingConstants.LEFT);
        gbc2.gridy = 3; currentStatePanel.add(labelQValAction2, gbc2);
        JLabel labelQValAction3 = new JLabel("[3] Action - GR: ", SwingConstants.LEFT);
        gbc2.gridy = 4; currentStatePanel.add(labelQValAction3, gbc2);
        JLabel labelQValAction4 = new JLabel("[4] Action - CL: ", SwingConstants.LEFT);
        gbc2.gridy = 5; currentStatePanel.add(labelQValAction4, gbc2);
        JLabel labelQValAction5 = new JLabel("[5] Action - SH: ", SwingConstants.LEFT);
        gbc2.gridy = 6; currentStatePanel.add(labelQValAction5, gbc2);
        JLabel labelQValActionBest = new JLabel("Best Action: ", SwingConstants.LEFT);
        gbc2.gridy = 7; currentStatePanel.add(labelQValActionBest, gbc2);
        
        // State text field
        stateField = new JTextField();
        stateField.setEditable(false);
        gbc2.gridx = 1; gbc2.gridy = 0; gbc2.weightx = 1.0;
        currentStatePanel.add(stateField, gbc2);        
        
        // QValue text fields
        stateQValueFields = new JTextField[6];
        for (int i = 0; i < 6; i++) {
            stateQValueFields[i] = new JTextField();
            stateQValueFields[i].setEditable(false);
            gbc2.gridx = 1; gbc2.gridy = i+1; gbc2.weightx = 1.0;
            currentStatePanel.add(stateQValueFields[i], gbc2);            
        }
        
        // Best Action text field
        stateBestAction = new JTextField();
        stateBestAction.setEditable(false);
        gbc2.gridx = 1; gbc2.gridy = 7; gbc2.weightx = 1.0;
        currentStatePanel.add(stateBestAction, gbc2);    

        // Add sub-panel to main stats panel
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        qstatsPanel.add(currentStatePanel, gbc);
        
        return qstatsPanel;
    }
    
    private JPanel initTrainingUI() {
        // Create Training Panel
        JPanel trainingPanel = new JPanel();
        trainingPanel.setBackground(Color.LIGHT_GRAY);
        trainingPanel.setLayout(new GridBagLayout());
        
        // Set GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 1, 5, 1);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Create border title
        TitledBorder border = new TitledBorder(new LineBorder(Color.BLACK), "Training");
        border.setTitleJustification(TitledBorder.LEFT);
        border.setTitlePosition(TitledBorder.TOP);
        trainingPanel.setBorder(border);
        
        // === Settings UI ===
        // = LABELS =
        gbc.gridx = 0; gbc.weightx = 0.0; gbc.weighty = 0.0;
        // Label - Train Episodes
        JLabel labelTrainEpisodes = new JLabel("Train episodes per map: ", SwingConstants.LEFT);    
        gbc.gridy = 0; trainingPanel.add(labelTrainEpisodes, gbc);
        
        // = TEXT FIELDS =
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 0.0;        
        // TextField - Train Episodes
        trainEpisodesField = new JTextField("1000000");
        gbc.gridy = 0; trainingPanel.add(trainEpisodesField, gbc);
        
        // = BUTTONS =
        gbc.gridx = 0; gbc.weightx = 1.0; gbc.weighty = 0.0; gbc.gridwidth = 2;
        // Button - Train: Current map
        JButton trainSelectedBtn = new JButton("Train: Selected map");
        trainSelectedBtn.setActionCommand("TRAIN_SELECTED");
        trainSelectedBtn.addActionListener(this);
        gbc.gridy = 1; trainingPanel.add(trainSelectedBtn, gbc);        
        // Button - Train: Premade maps
        JButton trainPremadeBtn = new JButton("Train: Premade maps");
        trainPremadeBtn.setActionCommand("TRAIN_PREMADE");
        trainPremadeBtn.addActionListener(this);
        gbc.gridy = 2; trainingPanel.add(trainPremadeBtn, gbc);
        
        return trainingPanel;
    }
    
    private void refreshStats() 
    {
        QTable qt = QTable.getInstance();
        QState currentState = qt.getQStateFromWorld(w);
        int qstateCount = qt.getQStateCount();
        int bestAction = currentState.argmaxAction();
        
        // Update QTable count field
        qtableCountField.setText(Integer.toString(qstateCount));
        
        // Update Current state
        stateField.setText(Arrays.toString(currentState.getKey().getBytes(StandardCharsets.US_ASCII)));
        
        // Update Current QState value fields
        Double[] actualQValues = currentState.getQValues();
        for (int i = 0; i < 6; i++) {
            Double qval = actualQValues[i];
            stateQValueFields[i].setText(qval.toString());       
        }
        
        // Update Best Action field
        stateBestAction.setText(Integer.toString(bestAction));
        
        // Debug printout
        System.out.println();
        System.out.printf("IsInPit: %d, D: %d, X: %d, Y: %d, HasArrow: %d\n", 
                currentState.hasFallenIntoPit, currentState.playerD, currentState.playerX, currentState.playerY, currentState.hasArrow);
        System.out.println("State: " + Arrays.toString(currentState.getKey().getBytes(StandardCharsets.US_ASCII)));
        System.out.println("QValues (raw): " + Arrays.toString(currentState.actionQValues));
    }
    
    /**
     * Button commands.
     * 
     * @param e Button event.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("TL"))
        {
            w.doAction(World.A_TURN_LEFT);            
            updateGame();
            refreshStats();
        }
        if (e.getActionCommand().equals("TR"))
        {
            w.doAction(World.A_TURN_RIGHT);
            updateGame();
            refreshStats();
        }
        if (e.getActionCommand().equals("MF"))
        {
            w.doAction(World.A_MOVE);
            updateGame();
            refreshStats();
        }
        if (e.getActionCommand().equals("GRAB"))
        {
            w.doAction(World.A_GRAB);
            updateGame();
            refreshStats();
        }
        if (e.getActionCommand().equals("CLIMB"))
        {
            w.doAction(World.A_CLIMB);
            updateGame();
            refreshStats();
        }
        if (e.getActionCommand().equals("SHOOT"))
        {
            w.doAction(World.A_SHOOT);
            updateGame();
            refreshStats();
        }
        if (e.getActionCommand().equals("NEW"))
        {
            String s = (String)mapList.getSelectedItem();
            if (s.equalsIgnoreCase("Random"))
            {
                w = MapGenerator.getRandomMap((int)System.currentTimeMillis()).generateWorld();
            }
            else
            {
                int i = Integer.parseInt(s);
                i--;
                w = maps.get(i).generateWorld();
            }
            agent = new MyAgent(w);
            updateGame();
            refreshStats();
        }
        if (e.getActionCommand().equals("AGENT"))
        {
            if (agent == null)
            {
                agent = new MyAgent(w);
            }
            agent.doAction();
            updateGame();
            refreshStats();
        }
        if (e.getActionCommand().equals("TRAIN_SELECTED") || 
            e.getActionCommand().equals("TRAIN_PREMADE"))
        {
            WorldMap map;
            Double epsStart = 0.9;
            Double epsMin = 0.1;
            Double eps = 0.9;                      
            int trainEpisodes;
            
            // Parse training params
            try {
                String trainEpStr = trainEpisodesField.getText();
                trainEpStr = trainEpStr.replaceAll("\\s+", "");
                trainEpisodes = Integer.parseInt(trainEpStr);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Invalid integer format for 'Train episodes' field.", "Training error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Additional validation
            if (trainEpisodes < 1) {
                JOptionPane.showMessageDialog(frame, "'Train episodes' field must contain integer value above 0.", "Training error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Setup epsilon decay
            Double epsDecay = 1.0 / (trainEpisodes * 0.8);
            
            // === TRAINING ===
            System.out.println("=== TRAINING STARTED ===");
            
            if (e.getActionCommand().equals("TRAIN_SELECTED")) {
                // Current map training
                System.out.printf("> Started training on current map ...\n");
                String s = (String)mapList.getSelectedItem();
                if (s.equalsIgnoreCase("Random"))
                {
                    map = MapGenerator.getRandomMap((int)System.currentTimeMillis());
                }
                else
                {
                    int mapId = Integer.parseInt(s) - 1;
                    map = maps.get(mapId);
                }                
                
                eps = epsStart;
                for (int j = 0; j < trainEpisodes; j++) {
                    eps = eps - epsDecay;
                    eps = (eps < epsMin) ? 0.1 : eps;
                    env.trainEpisode(eps, map);
                }
                
                System.out.printf("> ... done training through %d episodes on current map\n", trainEpisodes);
            } else if (e.getActionCommand().equals("TRAIN_PREMADE")) {
                // Premade map training                
                for (int i = 0; i < 7; i++) {
                    System.out.printf("> Started training on premade map %d/7 ...\n", i+1);
                    map = maps.get(i);

                    for (int j = 0; j < trainEpisodes; j++) {
                        eps = eps - epsDecay;
                        eps = (eps < epsMin) ? 0.1 : eps;
                        env.trainEpisode(eps, map);
                    }
                    System.out.printf("> ... done training through %d episodes on premade map %d/7\n", trainEpisodes, i+1);
                }
            }
                      
            refreshStats();
        }
    }
    
    /**
     * Updates the game GUI to a new world state.
     */
    private void updateGame()
    {
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                blocks[i][j].removeAll();
                blocks[i][j].setBackground(Color.WHITE);
                if (w.hasPit(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_pit));
                }
                if (w.hasBreeze(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_breeze));
                }
                if (w.hasStench(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_stench));
                }
                if (w.hasWumpus(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_wumpus));
                }
                if (w.hasGlitter(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_glitter));
                }
                if (w.hasPlayer(i+1, j+1))
                {
                    if (w.getDirection() == World.DIR_DOWN) blocks[i][j].add(new JLabel(l_player_down));
                    if (w.getDirection() == World.DIR_UP) blocks[i][j].add(new JLabel(l_player_up));
                    if (w.getDirection() == World.DIR_LEFT) blocks[i][j].add(new JLabel(l_player_left));
                    if (w.getDirection() == World.DIR_RIGHT) blocks[i][j].add(new JLabel(l_player_right));
                }
                if (w.isUnknown(i+1, j+1))
                {
                    blocks[i][j].setBackground(Color.GRAY);
                }
                
                blocks[i][j].updateUI();
                blocks[i][j].repaint();
            }
        }
        
        score.setText("Score: " + w.getScore());
        status.setText("");
        if (w.isInPit())
        {
            status.setText("Player must climb up!");
        }
        if (w.gameOver())
        {
            status.setText("GAME OVER");
        }
        
        gamepanel.updateUI();
        gamepanel.repaint();
    }  
}
