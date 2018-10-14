/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mp3player;

import Utils.FileUtils;
import Utils.Mp3FileFilter;
import com.jtattoo.plaf.bernstein.BernsteinLookAndFeel;
import com.jtattoo.plaf.noire.NoireLookAndFeel;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

/**
 *
 * @author Даулет
 */
public class Frame extends javax.swing.JFrame implements BasicPlayerListener{
    
    private static final String MP3_FILE_EXTENSION = "mp3";
    private static final String MP3_FILE_DESCRIPTION = "Файлы mp3";
    private static final String PLAYLIST_FILE_EXTENSION = "pls";
    private static final String PLAYLIST_FILE_DESCRIPTION = "Файлы плейлиста";
    private static final String EMPTY_STRING = "";
    //private static final String INPUT_SONG_NAME = "введите имя песни";
    
    private DefaultListModel mp3ListModel=new DefaultListModel();
    private FileFilter mp3FileFilter=new Mp3FileFilter(MP3_FILE_EXTENSION,MP3_FILE_DESCRIPTION); 
    private FileFilter playlistFileFilter=new Mp3FileFilter(PLAYLIST_FILE_EXTENSION,PLAYLIST_FILE_DESCRIPTION); 
    private MP3Player player=new MP3Player(this);

    private long secondsAmount; // сколько секунд прошло с начала проигрывания
    private long duration; // длительность песни в секундах
    private int bytesLen; // размер песни в байтах
    private double posValue = 0.0; // позиция для прокрутки
    // передвигается ли ползунок песни от перетаскивания (или от проигрывания) - используется во время перемотки
    private boolean movingFromJump = false;
    private boolean moveAutomatic = false;// во время проигрывании песни ползунок передвигается, moveAutomatic = true
    
    private int durationMinutes;
    private int durationSeconds;
    private int secondsAmountMinutes;
    private int secondsAmountSeconds;
    
     
    /**
     * Creates new form Frame
     * @param o     
     * @param map     
     */
    
    @Override
    public void opened(Object o, Map map) {
        
//        еще один вариант определения mp3 тегов
//        AudioFileFormat aff = null;
//        try {
//            aff = AudioSystem.getAudioFileFormat(new File(o.toString()));
//        } catch (UnsupportedAudioFileException ex) {
//            Logger.getLogger(MP3PlayerGui.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(MP3PlayerGui.class.getName()).log(Level.SEVERE, null, ex);
//        }
               
        // определить длину песни и размер файла
        duration = (long) Math.round((((Long) map.get("duration")).longValue()) / 1000000);
        bytesLen = (int) Math.round(((Integer) map.get("mp3.length.bytes")).intValue());
        
        // если есть mp3 тег для имени - берем его, если нет - вытаскиваем название из имени файла
        //String songName = map.get("title") != null ? map.get("title").toString() : FileUtils.getFileNameWithoutExtension(new File(o.toString()).getName());
        String songName =FileUtils.getFileNameWithoutExtension(new File(o.toString()).getName());
        // если длинное название - укоротить его
        if (songName.length() > 40) {
            songName = songName.substring(0, 40) + "...";
        }
        labelSongName.setText(songName);  
        double a=duration/60;
        durationMinutes=(int)a;
        double b=durationMinutes*60;
        durationSeconds=(int)Math.round(duration-b);
    }

    @Override
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
       float progress = -1.0f;

        if ((bytesread > 0) && ((duration > 0))) {
            progress = bytesread * 1.0f / bytesLen * 1.0f;
        }


        // сколько секунд прошло
        
        secondsAmount = (long) (duration * progress);
        
        double a=secondsAmount/60;
        secondsAmountMinutes=(int)a;
        double b=secondsAmountMinutes*60;
        secondsAmountSeconds=(int)Math.round(secondsAmount-b);
        //System.out.println(secondsAmountMinutes+" "+secondsAmountSeconds+"/"+durationMinutes+" "+durationSeconds);
        if (durationSeconds<10&&secondsAmountSeconds>=10){
            labelProgress.setText(secondsAmountMinutes+"."+secondsAmountSeconds+"/"+durationMinutes+".0"+durationSeconds);
        }
        else if (durationSeconds<10&&secondsAmountSeconds<10){
            labelProgress.setText(secondsAmountMinutes+".0"+secondsAmountSeconds+"/"+durationMinutes+".0"+durationSeconds);
        }
        else if (durationSeconds>=10&&secondsAmountSeconds<10){
            labelProgress.setText(secondsAmountMinutes+".0"+secondsAmountSeconds+"/"+durationMinutes+"."+durationSeconds);
        }
        else {
            labelProgress.setText(secondsAmountMinutes+"."+secondsAmountSeconds+"/"+durationMinutes+"."+durationSeconds);
        }
        if (duration != 0) {
            if (movingFromJump == false) {
                sliderProgress.setValue(((int) Math.round(secondsAmount * 1000 / duration)));

            }
        } 
    }

    @Override
    public void stateUpdated(BasicPlayerEvent bpe) {
        int state = bpe.getCode();

        if (state == BasicPlayerEvent.PLAYING) {
            movingFromJump = false;
        } else if (state == BasicPlayerEvent.SEEKING) {
            movingFromJump = true;
        } else if (state == BasicPlayerEvent.EOM) {
            if (selectNextSong()) {
                playFile();
            }
        }
    }

    @Override
    public void setController(BasicController bc) {
        
    }
    
    public Frame() {
        
        initComponents();
        this.setIconImage(new ImageIcon("mp3.jpg").getImage());
        lstPlayList.grabFocus();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuOpen = new javax.swing.JMenuItem();
        jMenuClose = new javax.swing.JMenuItem();
        jMenuAdd = new javax.swing.JMenuItem();
        jMenuDel = new javax.swing.JMenuItem();
        jMenuClear = new javax.swing.JMenuItem();
        panelSearch = new javax.swing.JPanel();
        fieldSearch = new javax.swing.JTextField();
        butSearch = new javax.swing.JButton();
        panelMain = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        butAdd = new javax.swing.JButton();
        butDel = new javax.swing.JButton();
        butSelNext = new javax.swing.JButton();
        javax.swing.JButton butSelPrev = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstPlayList = new javax.swing.JList();
        btnMute = new javax.swing.JToggleButton();
        sliderVol = new javax.swing.JSlider();
        butNext = new javax.swing.JButton();
        butPrev = new javax.swing.JButton();
        butPlay = new javax.swing.JButton();
        butPaus = new javax.swing.JButton();
        butStop = new javax.swing.JButton();
        sliderProgress = new javax.swing.JSlider();
        labelSongName = new javax.swing.JLabel();
        labelProgress = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuOpen = new javax.swing.JMenuItem();
        menuSave = new javax.swing.JMenuItem();
        jMenuClear1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuExit = new javax.swing.JMenuItem();
        menuService = new javax.swing.JMenu();
        menuSkins = new javax.swing.JMenu();
        menuSkin1 = new javax.swing.JMenuItem();
        menuSkin2 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        menuSkinDefault = new javax.swing.JMenuItem();

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(true);

        jMenuOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-icon.png"))); // NOI18N
        jMenuOpen.setText("Открыть плейлист");
        jMenuOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuOpen);

        jMenuClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/CloseIcon.png"))); // NOI18N
        jMenuClose.setText("Закрыть плейлист");
        jMenuClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuClearActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuClose);

        jMenuAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plus_16.png"))); // NOI18N
        jMenuAdd.setText("Добавить трек");
        jMenuAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAddActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuAdd);

        jMenuDel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/remove_icon.png"))); // NOI18N
        jMenuDel.setText("Удалить трек");
        jMenuDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDelActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuDel);

        jMenuClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/3461_3929_edit-clear_16x16.png"))); // NOI18N
        jMenuClear.setText("Очистить список");
        jMenuClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuClearActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuClear);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MP3 проигрыватель");
        setResizable(false);

        panelSearch.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        fieldSearch.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        fieldSearch.setForeground(new java.awt.Color(102, 102, 102));
        fieldSearch.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        fieldSearch.setText("введите название трека");
        fieldSearch.setToolTipText("");
        fieldSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        fieldSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldFocusGain(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fieldFocusLost(evt);
            }
        });

        butSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/search_16.png"))); // NOI18N
        butSearch.setText("Найти");
        butSearch.setIconTextGap(10);
        butSearch.setInheritsPopupMenu(true);
        butSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSearchLayout = new javax.swing.GroupLayout(panelSearch);
        panelSearch.setLayout(panelSearchLayout);
        panelSearchLayout.setHorizontalGroup(
            panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(butSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelSearchLayout.setVerticalGroup(
            panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSearchLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butSearch))
                .addContainerGap())
        );

        panelMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        butAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plus_16.png"))); // NOI18N
        butAdd.setToolTipText("добавить трек");
        butAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAddActionPerformed(evt);
            }
        });

        butDel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/remove_icon.png"))); // NOI18N
        butDel.setToolTipText("удалить трек");
        butDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDelActionPerformed(evt);
            }
        });

        butSelNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow-down-icon.png"))); // NOI18N
        butSelNext.setToolTipText("следующий трек");
        butSelNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSelNextActionPerformed(evt);
            }
        });

        butSelPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow-up-icon.png"))); // NOI18N
        butSelPrev.setToolTipText("предыдущий трек");
        butSelPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSelPrevActionPerformed(evt);
            }
        });

        lstPlayList.setModel(mp3ListModel);
        lstPlayList.setToolTipText("");
        lstPlayList.setComponentPopupMenu(jPopupMenu);
        lstPlayList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mouseClickedAction(evt);
            }
        });
        jScrollPane1.setViewportView(lstPlayList);

        btnMute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/speaker.png"))); // NOI18N
        btnMute.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/mute.png"))); // NOI18N
        btnMute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMuteActionPerformed(evt);
            }
        });

        sliderVol.setPaintTicks(true);
        sliderVol.setSnapToTicks(true);
        sliderVol.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderVolStateChanged(evt);
            }
        });

        butNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/next-icon.png"))); // NOI18N
        butNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNextActionPerformed(evt);
            }
        });

        butPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/prev-icon.png"))); // NOI18N
        butPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPrevActionPerformed(evt);
            }
        });

        butPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Play.png"))); // NOI18N
        butPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPlayActionPerformed(evt);
            }
        });

        butPaus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Pause-icon.png"))); // NOI18N
        butPaus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPausActionPerformed(evt);
            }
        });

        butStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop-red-icon.png"))); // NOI18N
        butStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butStopActionPerformed(evt);
            }
        });

        sliderProgress.setMaximum(1000);
        sliderProgress.setMinorTickSpacing(1);
        sliderProgress.setSnapToTicks(true);
        sliderProgress.setToolTipText("");
        sliderProgress.setValue(0);
        sliderProgress.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderProgressChange(evt);
            }
        });

        labelSongName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelSongName.setText("...");
        labelSongName.setToolTipText("");

        labelProgress.setText("0/0");

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sliderProgress, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelMainLayout.createSequentialGroup()
                                .addComponent(btnMute)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sliderVol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelMainLayout.createSequentialGroup()
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelMainLayout.createSequentialGroup()
                                        .addComponent(butPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(12, 12, 12)
                                        .addComponent(butPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(butPaus, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(butStop, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(butNext, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelMainLayout.createSequentialGroup()
                                        .addComponent(butAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(butDel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(10, 10, 10)
                                        .addComponent(butSelNext, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(butSelPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addComponent(labelSongName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelProgress)
                        .addGap(29, 29, 29))))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(butSelPrev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(butSelNext, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(butDel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(butAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSongName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelProgress))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sliderProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnMute, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sliderVol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(butNext, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butPaus, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butStop, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        menuFile.setText("Файл");

        menuOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-icon.png"))); // NOI18N
        menuOpen.setText("Открыть плейлист");
        menuOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenActionPerformed(evt);
            }
        });
        menuFile.add(menuOpen);

        menuSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_16.png"))); // NOI18N
        menuSave.setText("Сохранить плейлист");
        menuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveActionPerformed(evt);
            }
        });
        menuFile.add(menuSave);

        jMenuClear1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/3461_3929_edit-clear_16x16.png"))); // NOI18N
        jMenuClear1.setText("Очистить список");
        jMenuClear1.setToolTipText("");
        jMenuClear1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuClearActionPerformed(evt);
            }
        });
        menuFile.add(jMenuClear1);
        menuFile.add(jSeparator1);

        menuExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/exit.png"))); // NOI18N
        menuExit.setText("Выход");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        menuFile.add(menuExit);

        menuBar.add(menuFile);

        menuService.setText("Сервис");

        menuSkins.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/gear_16.png"))); // NOI18N
        menuSkins.setText("Внешний вид");
        menuSkins.setToolTipText("");

        menuSkin1.setText("Скин 1");
        menuSkin1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skin1(evt);
            }
        });
        menuSkins.add(menuSkin1);

        menuSkin2.setText("Скин 2");
        menuSkin2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skin2(evt);
            }
        });
        menuSkins.add(menuSkin2);
        menuSkins.add(jSeparator3);

        menuSkinDefault.setText("По умолчанию");
        menuSkinDefault.setToolTipText("");
        menuSkinDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skinDefault(evt);
            }
        });
        menuSkins.add(menuSkinDefault);

        menuService.add(menuSkins);

        menuBar.add(menuService);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void skin1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skin1
         
        try {
            UIManager.setLookAndFeel(new NoireLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
               
        SwingUtilities.updateComponentTreeUI(this);
        //this.setVisible(true);
    }//GEN-LAST:event_skin1

    private void skin2(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skin2
        try {
            UIManager.setLookAndFeel(new BernsteinLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
        SwingUtilities.updateComponentTreeUI(this);
    }//GEN-LAST:event_skin2

    private void skinDefault(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skinDefault
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
        SwingUtilities.updateComponentTreeUI(this);
    }//GEN-LAST:event_skinDefault

    private void fieldFocusGain(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldFocusGain
if ("введите название трека".equals(fieldSearch.getText())){
    fieldSearch.setText("");
    fieldSearch.setForeground(Color.BLACK);
}
    }//GEN-LAST:event_fieldFocusGain

    private void fieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldFocusLost
        if ("".equals(fieldSearch.getText())){
    fieldSearch.setText("введите название трека");
    fieldSearch.setForeground(Color.GRAY);
        }
    }//GEN-LAST:event_fieldFocusLost

    private void butAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAddActionPerformed
        FileUtils.addFileFilter(fileChooser, mp3FileFilter);
        int result = fileChooser.showOpenDialog(this);// result хранит результат: выбран файл или нет

        if (result == JFileChooser.APPROVE_OPTION) {// если нажата клавиша OK или YES

            File[] selectedFiles = fileChooser.getSelectedFiles();
            // перебираем все выделенные файлы для добавления в плейлист
            for (File file : selectedFiles) {
                MP3 mp3 = new MP3(file.getName(), file.getPath());
                
                // если эта песня уже есть в списке - не добавлять ее
                if (!mp3ListModel.contains(mp3)) mp3ListModel.addElement(mp3);
            }

        }
    }//GEN-LAST:event_butAddActionPerformed

    private void butDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDelActionPerformed
        int[] indexPlayList = lstPlayList.getSelectedIndices();// получаем выбранные индексы(порядковый номер) песен
        if (indexPlayList.length > 0) {// если выбрали хотя бы одну песню
            ArrayList<MP3> mp3ListForRemove = new ArrayList<MP3>();// сначала сохраняем все mp3 для удаления в отдельную коллекцию
            for (int i = 0; i < indexPlayList.length; i++) {// создаем список удаляемых
                MP3 mp3 = (MP3) mp3ListModel.getElementAt(indexPlayList[i]);
                mp3ListForRemove.add(mp3);
            }

            // удаляем mp3 в плейлисте
            for (MP3 mp3 : mp3ListForRemove) {
                mp3ListModel.removeElement(mp3);
            }
           
        }
        lstPlayList.setSelectedIndex(0);
    }//GEN-LAST:event_butDelActionPerformed

    private void butSelNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSelNextActionPerformed
        if (!lstPlayList.isFocusOwner()) {
            lstPlayList.grabFocus();            
        }
        int nextIndex=lstPlayList.getSelectedIndex()+1;
        if (nextIndex<=lstPlayList.getModel().getSize()){
            lstPlayList.setSelectedIndex(nextIndex);
        }
    }//GEN-LAST:event_butSelNextActionPerformed

    private void butSelPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSelPrevActionPerformed
        if (!lstPlayList.isFocusOwner()) {
            lstPlayList.grabFocus();
        }
        int prevIndex=lstPlayList.getSelectedIndex()-1;
        if (prevIndex>=0){
            lstPlayList.setSelectedIndex(prevIndex);
        }
    }//GEN-LAST:event_butSelPrevActionPerformed

    private void menuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
 FileUtils.addFileFilter(fileChooser, playlistFileFilter);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {// если нажата клавиша OK или YES
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.exists()) {// если такой файл уже существует

                int resultOvveride = JOptionPane.showConfirmDialog(this, "Файл существует", "Перезаписать?", JOptionPane.YES_NO_CANCEL_OPTION);
                switch (resultOvveride) {
                    case JOptionPane.NO_OPTION:
                        menuSaveActionPerformed(evt);// повторно открыть окно сохранения файла
                        return;
                    case JOptionPane.CANCEL_OPTION:
                        fileChooser.cancelSelection();
                        return;
                }
                fileChooser.approveSelection();
            }

            String fileExtension = FileUtils.getFileExtension(selectedFile);

            // имя файла (нужно ли добавлять раширение к имени файлу при сохранении)
            String fileNameForSave = (fileExtension != null && fileExtension.equals(PLAYLIST_FILE_EXTENSION)) ? selectedFile.getPath() : selectedFile.getPath() + "." + PLAYLIST_FILE_EXTENSION;

            FileUtils.serialize(mp3ListModel, fileNameForSave);
        }
    }//GEN-LAST:event_menuSaveActionPerformed

    private void menuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenActionPerformed
        FileUtils.addFileFilter(fileChooser, playlistFileFilter);
        int result = fileChooser.showOpenDialog(this);// result хранит результат: выбран файл или нет


        if (result == JFileChooser.APPROVE_OPTION) {// если нажата клавиша OK или YES
            File selectedFile = fileChooser.getSelectedFile();// 
            DefaultListModel mp3ListModel = (DefaultListModel) FileUtils.deserialize(selectedFile.getPath());
            this.mp3ListModel = mp3ListModel;
            lstPlayList.setModel(mp3ListModel);
        }
    }//GEN-LAST:event_menuOpenActionPerformed

    private void butSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSearchActionPerformed
        String searchStr = fieldSearch.getText();

        // если в поиске ничего не ввели - выйти из метода и не производить поиск
        if (searchStr == null || searchStr.trim().equals(EMPTY_STRING)) {
            return;
        }

        // все индексы объектов, найденных по поиску, будут храниться в коллекции
        ArrayList<Integer> mp3FindedIndexes = new ArrayList<Integer>();

        // проходим по коллекции и ищем соответствия имен песен со строкой поиска
        for (int i = 0; i < mp3ListModel.size(); i++) {
            MP3 mp3 = (MP3) mp3ListModel.getElementAt(i);
            // поиск вхождения строки в название песни без учета регистра букв
            if (mp3.getName().toUpperCase().contains(searchStr.toUpperCase())) {
                mp3FindedIndexes.add(i);// найденный индексы добавляем в коллекцию
            }
        }

        // коллекцию индексов сохраняем в массив
        int[] selectIndexes = new int[mp3FindedIndexes.size()];

        if (selectIndexes.length == 0) {// если не найдено ни одной песни, удовлетворяющей условию поиска
            JOptionPane.showMessageDialog(this, "Поиск по строке \'" + searchStr + "\' не дал результатов");
            fieldSearch.requestFocus();
            fieldSearch.selectAll();
            return;
        }

        // преобразовать коллекцию в массив, т.к. метод для выделения строк в JList работает только с массивом
        for (int i = 0; i < selectIndexes.length; i++) {
            selectIndexes[i] = mp3FindedIndexes.get(i).intValue();
        }

        // выделить в плелисте найдные песни по массиву индексов, найденных ранее
        lstPlayList.setSelectedIndices(selectIndexes);
    }//GEN-LAST:event_butSearchActionPerformed

    private void butPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPlayActionPerformed
        playFile();        
        
    }//GEN-LAST:event_butPlayActionPerformed

    private void jMenuClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuClearActionPerformed
        mp3ListModel.clear();
    }//GEN-LAST:event_jMenuClearActionPerformed

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_menuExitActionPerformed

    private void butPausActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPausActionPerformed
        player.pause();
    }//GEN-LAST:event_butPausActionPerformed

    private void butStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butStopActionPerformed
        player.stop();
    }//GEN-LAST:event_butStopActionPerformed

    private void sliderVolStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderVolStateChanged
        player.setVolume(sliderVol.getValue(),sliderVol.getMaximum());
        
        if (sliderVol.getValue()==0){
            btnMute.setSelected(true);
        }else {
            btnMute.setSelected(false);
        }
    }//GEN-LAST:event_sliderVolStateChanged
    private int currentVolumeValue;
    private void btnMuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMuteActionPerformed
        if (btnMute.isSelected()){
            currentVolumeValue = sliderVol.getValue();
            sliderVol.setValue(0);
        }else{
            sliderVol.setValue(currentVolumeValue);
        }
    }//GEN-LAST:event_btnMuteActionPerformed

    private void butPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrevActionPerformed
        butSelPrevActionPerformed(evt);
        butPlayActionPerformed(evt);
    }//GEN-LAST:event_butPrevActionPerformed

    private void butNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNextActionPerformed
        butSelNextActionPerformed(evt);
        butPlayActionPerformed(evt);
    }//GEN-LAST:event_butNextActionPerformed

    private void mouseClickedAction(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseClickedAction
        // если нажали левую кнопку мыши 2 раза
        if (evt.getModifiers() == InputEvent.BUTTON1_MASK && evt.getClickCount() == 2){
            int[] indexPlayList = lstPlayList.getSelectedIndices();
            if (indexPlayList.length > 0) {
                MP3 mp3 = (MP3) mp3ListModel.getElementAt(indexPlayList[0]);
                player.play(mp3.getPath());
                player.setVolume(sliderVol.getValue(),sliderVol.getMaximum());
            }
        }
    }//GEN-LAST:event_mouseClickedAction

    private void sliderProgressChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderProgressChange
        if (sliderProgress.getValueIsAdjusting() == false) {
            if (moveAutomatic == true) {
                moveAutomatic = false;
                posValue = sliderProgress.getValue() * 1.0 / 1000;
                processSeek(posValue);
            }
        } else {
            moveAutomatic = true;
            movingFromJump = true;
        }
    }//GEN-LAST:event_sliderProgressChange

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
                    
        JFrame.setDefaultLookAndFeelDecorated(true);
        try {
            //</editor-fold>
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
              new Frame().setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnMute;
    private javax.swing.JButton butAdd;
    private javax.swing.JButton butDel;
    private javax.swing.JButton butNext;
    private javax.swing.JButton butPaus;
    private javax.swing.JButton butPlay;
    private javax.swing.JButton butPrev;
    private javax.swing.JButton butSearch;
    private javax.swing.JButton butSelNext;
    private javax.swing.JButton butStop;
    private javax.swing.JTextField fieldSearch;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JMenuItem jMenuAdd;
    private javax.swing.JMenuItem jMenuClear;
    private javax.swing.JMenuItem jMenuClear1;
    private javax.swing.JMenuItem jMenuClose;
    private javax.swing.JMenuItem jMenuDel;
    private javax.swing.JMenuItem jMenuOpen;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JLabel labelProgress;
    private javax.swing.JLabel labelSongName;
    private javax.swing.JList lstPlayList;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuOpen;
    private javax.swing.JMenuItem menuSave;
    private javax.swing.JMenu menuService;
    private javax.swing.JMenuItem menuSkin1;
    private javax.swing.JMenuItem menuSkin2;
    private javax.swing.JMenuItem menuSkinDefault;
    private javax.swing.JMenu menuSkins;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelSearch;
    private javax.swing.JSlider sliderProgress;
    private javax.swing.JSlider sliderVol;
    // End of variables declaration//GEN-END:variables

    
    private boolean selectNextSong() {
        int nextIndex = lstPlayList.getSelectedIndex() + 1;
        if (nextIndex <= lstPlayList.getModel().getSize() - 1) {// если не вышли за пределы плейлиста
            lstPlayList.setSelectedIndex(nextIndex);
            return true;
        }
        return false;
    }
    private void playFile() {
        
        int[] indexPlayList = lstPlayList.getSelectedIndices();// получаем выбранные индексы(порядковый номер) песен
        if (indexPlayList.length > 0) {// если выбрали хотя бы одну песню
            MP3 mp3 = (MP3) mp3ListModel.getElementAt(indexPlayList[0]);// находим первую выбранную песню (т.к. несколько песен нельзя проиграть одновременно
            player.play(mp3.getPath());
            player.setVolume(sliderVol.getValue(), sliderVol.getMaximum());
            //labelSongName.setText(mp3.getName());
        }
        else {
            lstPlayList.setSelectedIndex(0);
            MP3 mp3 = (MP3) mp3ListModel.getElementAt(0);
            player.play(mp3.getPath());
            player.setVolume(sliderVol.getValue(), sliderVol.getMaximum());
            //labelSongName.setText(mp3.getName());
        }
    }
    private void processSeek(double bytes) {
        try {
            long skipBytes = (long) Math.round(((Integer) bytesLen).intValue() * bytes);
            player.jump(skipBytes);
        } catch (Exception e) {
            e.printStackTrace();
            movingFromJump = false;
        }

    }
}
