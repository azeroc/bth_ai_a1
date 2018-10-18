package wumpusworld;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Vector;

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
    private World w;
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
        frame.setSize(1080, 640);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        gamepanel = new JPanel();
        gamepanel.setPreferredSize(new Dimension(600,600));
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
        frame.getContentPane().add(gamepanel);
        
        //Add buttons panel
        JPanel buttons = new JPanel();
        buttons.setPreferredSize(new Dimension(300,600));
        buttons.setLayout(new FlowLayout());
        //Status label
        status = new JLabel("", SwingConstants.CENTER);
        status.setPreferredSize(new Dimension(200,25));
        buttons.add(status);
        //Score label
        score = new JLabel("Score: 0", SwingConstants.CENTER);
        score.setPreferredSize(new Dimension(200,25));
        buttons.add(score);
        //Buttons
        JButton bl = new JButton(new ImageIcon("gfx/TL.png"));
        bl.setActionCommand("TL");
        bl.addActionListener(this);
        buttons.add(bl);
        JButton bf = new JButton(new ImageIcon("gfx/MF.png"));
        bf.setActionCommand("MF");
        bf.addActionListener(this);
        buttons.add(bf);
        JButton br = new JButton(new ImageIcon("gfx/TR.png"));
        br.setActionCommand("TR");
        br.addActionListener(this);
        buttons.add(br);
        JButton bg = new JButton("Grab");
        bg.setPreferredSize(new Dimension(80,22));
        bg.setActionCommand("GRAB");
        bg.addActionListener(this);
        buttons.add(bg);
        JButton bc = new JButton("Climb");
        bc.setPreferredSize(new Dimension(80,22));
        bc.setActionCommand("CLIMB");
        bc.addActionListener(this);
        buttons.add(bc);
        JButton bs = new JButton("Shoot");
        bs.setPreferredSize(new Dimension(80,22));
        bs.setActionCommand("SHOOT");
        bs.addActionListener(this);
        buttons.add(bs);
        JButton ba = new JButton("Run Solving Agent");
        ba.setActionCommand("AGENT");
        ba.addActionListener(this);
        buttons.add(ba);
        //Add a delimiter
        JLabel l = new JLabel("");
        l.setPreferredSize(new Dimension(200,25));
        buttons.add(l);
        //Fill dropdown list
        Vector<String> items = new Vector<String>();
        for (int i = 0; i < maps.size(); i++)
        {
            items.add((i+1) + "");
        }
        items.add("Random");
        mapList = new JComboBox(items);
        mapList.setPreferredSize(new Dimension(180,25));
        buttons.add(mapList);
        JButton bn = new JButton("New Game");
        bn.setActionCommand("NEW");
        bn.addActionListener(this);
        buttons.add(bn);
        
        frame.getContentPane().add(buttons);
        
        updateGame();
        
        //Show window
        frame.setVisible(true);
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
        }
        if (e.getActionCommand().equals("TR"))
        {
            w.doAction(World.A_TURN_RIGHT);
            updateGame();
        }
        if (e.getActionCommand().equals("MF"))
        {
            w.doAction(World.A_MOVE);
            updateGame();
        }
        if (e.getActionCommand().equals("GRAB"))
        {
            w.doAction(World.A_GRAB);
            updateGame();
        }
        if (e.getActionCommand().equals("CLIMB"))
        {
            w.doAction(World.A_CLIMB);
            updateGame();
        }
        if (e.getActionCommand().equals("SHOOT"))
        {
            w.doAction(World.A_SHOOT);
            updateGame();
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
        }
        if (e.getActionCommand().equals("AGENT"))
        {
            if (agent == null)
            {
                agent = new MyAgent(w);
            }
            agent.doAction();
            updateGame();
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
