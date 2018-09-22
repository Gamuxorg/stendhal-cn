/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.tools.npcparser;

import java.awt.Cursor;
import java.io.IOException;
import java.net.URI;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.CaseInsensitiveExprMatcher;
import games.stendhal.common.parser.ConvCtxForMatchingSource;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.ExactExprMatcher;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.ExpressionMatcher;
import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.parser.SimilarExprMatcher;
import games.stendhal.common.parser.WordList;
import games.stendhal.server.core.config.ZoneGroupsXMLLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import marauroa.server.game.db.DatabaseFactory;

/**
 * Dialog of the NPC Conversation Parser Test Environment.
 *
 * @author M. Fuchs
 */
@SuppressWarnings("serial")
public class TestEnvDlg extends javax.swing.JDialog {

	private static final Logger logger = Logger.getLogger(TestEnvDlg.class);

	/** Creates new form TestEnvDlg */
	public TestEnvDlg() {
		initComponents();
		getRootPane().setDefaultButton(btMatch);
		updateWordCount();
		lbUnknownWarning.setVisible(false);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btLoadEntities;
    private javax.swing.JButton btLoadWorld;
    private javax.swing.JButton btMatch;
    private javax.swing.JButton btParse;
    private javax.swing.JButton btWriteWordlist;
    private javax.swing.JComboBox<String> cbMatchExpr;
    private javax.swing.JComboBox<String> cbMatchType;
    private javax.swing.JComboBox<String> cbSentence;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbAnalyzed;
    private javax.swing.JLabel lbMatchResult;
    private javax.swing.JLabel lbMatching;
    private javax.swing.JLabel lbNormalized;
    private javax.swing.JLabel lbNumeral;
    private javax.swing.JLabel lbObject;
    private javax.swing.JLabel lbParsedMatchString;
    private javax.swing.JLabel lbParsedSentence;
    private javax.swing.JLabel lbSentence;
    private javax.swing.JLabel lbSubject;
    private javax.swing.JLabel lbToString;
    private javax.swing.JLabel lbTrigger;
    private javax.swing.JLabel lbTrimmed;
    private javax.swing.JLabel lbUnknownWarning;
    private javax.swing.JLabel lbVerb;
    private javax.swing.JLabel lbWordCount;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelMatch;
    private javax.swing.JPanel panelSentence;
    private javax.swing.JTextField tfNormalized;
    private javax.swing.JTextField tfNumeral;
    private javax.swing.JTextField tfObject;
    private javax.swing.JTextField tfParsedMatchString;
    private javax.swing.JTextField tfParsedSentence;
    private javax.swing.JTextField tfSubject;
    private javax.swing.JTextField tfToString;
    private javax.swing.JTextField tfTrigger;
    private javax.swing.JTextField tfTrimmed;
    private javax.swing.JTextField tfVerb;
    private javax.swing.JTextField tfWordCount;
    private javax.swing.JTextPane tpExpressions;
    // End of variables declaration//GEN-END:variables

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panelHeader = new javax.swing.JPanel();
        lbWordCount = new javax.swing.JLabel();
        tfWordCount = new javax.swing.JTextField();
        btLoadEntities = new javax.swing.JButton();
        btLoadWorld = new javax.swing.JButton();
        btWriteWordlist = new javax.swing.JButton();
        panelSentence = new javax.swing.JPanel();
        lbSentence = new javax.swing.JLabel();
        cbSentence = new javax.swing.JComboBox<String>();
        btParse = new javax.swing.JButton();
        tfTrimmed = new javax.swing.JTextField();
        lbTrimmed = new javax.swing.JLabel();
        lbNormalized = new javax.swing.JLabel();
        tfNormalized = new javax.swing.JTextField();
        lbToString = new javax.swing.JLabel();
        tfToString = new javax.swing.JTextField();
        lbTrigger = new javax.swing.JLabel();
        tfTrigger = new javax.swing.JTextField();
        lbSubject = new javax.swing.JLabel();
        tfSubject = new javax.swing.JTextField();
        lbNumeral = new javax.swing.JLabel();
        tfNumeral = new javax.swing.JTextField();
        lbObject = new javax.swing.JLabel();
        tfObject = new javax.swing.JTextField();
        lbVerb = new javax.swing.JLabel();
        tfVerb = new javax.swing.JTextField();
        lbAnalyzed = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tpExpressions = new javax.swing.JTextPane();
        lbUnknownWarning = new javax.swing.JLabel();
        panelMatch = new javax.swing.JPanel();
        lbMatchResult = new javax.swing.JLabel();
        lbMatching = new javax.swing.JLabel();
        cbMatchType = new javax.swing.JComboBox<String>();
        cbMatchExpr = new javax.swing.JComboBox<String>();
        lbParsedSentence = new javax.swing.JLabel();
        tfParsedSentence = new javax.swing.JTextField();
        lbParsedMatchString = new javax.swing.JLabel();
        tfParsedMatchString = new javax.swing.JTextField();
        btMatch = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("NPC Parser Test Environment");
        setBounds(new java.awt.Rectangle(49, 200, 0, 0));
        setLocation(new java.awt.Point(200, 200));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        panelHeader.setLayout(new java.awt.GridBagLayout());

        lbWordCount.setText("Registered words:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelHeader.add(lbWordCount, gridBagConstraints);

        tfWordCount.setEditable(false);
        tfWordCount.setText("0");
        tfWordCount.setMinimumSize(new java.awt.Dimension(50, 28));
        tfWordCount.setPreferredSize(new java.awt.Dimension(100, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelHeader.add(tfWordCount, gridBagConstraints);

        btLoadEntities.setText("load entities");
        btLoadEntities.setToolTipText("Load entities into word list.");
        btLoadEntities.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLoadEntitiesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        panelHeader.add(btLoadEntities, gridBagConstraints);

        btLoadWorld.setText("load world");
        btLoadWorld.setToolTipText("Load zone configurations and quests to complete the word list like in the Stendhal server.");
        btLoadWorld.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLoadWorldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        panelHeader.add(btLoadWorld, gridBagConstraints);

        btWriteWordlist.setText("write WL");
        btWriteWordlist.setToolTipText("Update word list in database and source code.");
        btWriteWordlist.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btWriteWordlistActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        panelHeader.add(btWriteWordlist, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(panelHeader, gridBagConstraints);

        panelSentence.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelSentence.setPreferredSize(new java.awt.Dimension(649, 490));

        lbSentence.setText("Please enter a sentence:");

        cbSentence.setEditable(true);
<<<<<<< HEAD
        cbSentence.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "buy seven bananas", "buy 3 cookies", "give a bottle of wine", "buy enhanced lion shield", "Would you like to have an ice cream?", "Mary has a little lamb.", "I and you, he and they", "What is the an answer to life, the universe and everything?", "to be or not to be", "Take these three 烤排s and have fun!", "99 red balloons", "Hi, how are you?", "_Hi, how are you?" }));
=======
        cbSentence.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "buy seven bananas", "buy 3 cookies", "give a bottle of 红酒", "buy enhanced lion shield", "Would you like to have an ice cream?", "Mary has a little lamb.", "I and you, he and they", "What is the an answer to life, the universe and everything?", "to be or not to be", "Take these three 烤排s and have fun!", "99 red balloons", "Hi, how are you?", "_Hi, how are you?" }));
>>>>>>> f76672e17df092a61ddb88a57859203a0a9ef0ae
        cbSentence.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSentenceActionPerformed(evt);
            }
        });

        btParse.setText("parse");
        btParse.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btParseActionPerformed(evt);
            }
        });

        tfTrimmed.setEditable(false);

        lbTrimmed.setText("Normalized:");

        lbNormalized.setText("Trimmed:");

        tfNormalized.setEditable(false);

        lbToString.setText("toString():");

        tfToString.setEditable(false);

        lbTrigger.setText("Trigger:");

        tfTrigger.setEditable(false);

        lbSubject.setText("Subject:");

        tfSubject.setEditable(false);

        lbNumeral.setText("Numeral:");

        tfNumeral.setEditable(false);

        lbObject.setText("Object:");

        tfObject.setEditable(false);

        lbVerb.setText("Verb:");

        tfVerb.setEditable(false);

        lbAnalyzed.setText("Expressions:");

        tpExpressions.setEditable(false);
        jScrollPane1.setViewportView(tpExpressions);

        lbUnknownWarning.setForeground(java.awt.Color.red);
        lbUnknownWarning.setText("Please add an entry for all UNKNOWN words in red expressions to the NPC parser word list!");

        org.jdesktop.layout.GroupLayout panelSentenceLayout = new org.jdesktop.layout.GroupLayout(panelSentence);
        panelSentence.setLayout(panelSentenceLayout);
        panelSentenceLayout.setHorizontalGroup(
            panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelSentenceLayout.createSequentialGroup()
                .addContainerGap()
                .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelSentenceLayout.createSequentialGroup()
                        .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lbNormalized)
                            .add(lbSentence)
                            .add(lbTrimmed)
                            .add(lbToString)
                            .add(cbSentence, 0, 493, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btParse))
                    .add(tfTrimmed, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 577, Short.MAX_VALUE)
                    .add(tfNormalized, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 577, Short.MAX_VALUE)
                    .add(tfToString, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 577, Short.MAX_VALUE))
                .addContainerGap())
            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelSentenceLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(panelSentenceLayout.createSequentialGroup()
                            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 243, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(lbAnalyzed))
                            .add(27, 27, 27)
                            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(lbSubject)
                                .add(lbVerb)
                                .add(lbNumeral)
                                .add(lbObject)
                                .add(lbTrigger))
                            .add(18, 18, 18)
                            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(tfSubject, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                                .add(tfVerb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                                .add(tfNumeral, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                                .add(tfObject, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                                .add(tfTrigger, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)))
                        .add(lbUnknownWarning))
                    .addContainerGap()))
        );
        panelSentenceLayout.setVerticalGroup(
            panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelSentenceLayout.createSequentialGroup()
                .addContainerGap()
                .add(lbSentence)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbSentence, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btParse))
                .add(10, 10, 10)
                .add(lbNormalized)
                .add(2, 2, 2)
                .add(tfNormalized, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(lbTrimmed)
                .add(2, 2, 2)
                .add(tfTrimmed, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(lbToString)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tfToString, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(245, Short.MAX_VALUE))
            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelSentenceLayout.createSequentialGroup()
                    .add(264, 264, 264)
                    .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(panelSentenceLayout.createSequentialGroup()
                            .add(lbAnalyzed)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(panelSentenceLayout.createSequentialGroup()
                            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(tfSubject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(lbSubject))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(tfVerb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(lbVerb))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(tfNumeral, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(lbNumeral))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(tfObject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(lbObject))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(panelSentenceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(tfTrigger, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(lbTrigger))))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(lbUnknownWarning)
                    .addContainerGap(43, Short.MAX_VALUE)))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(panelSentence, gridBagConstraints);

        panelMatch.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelMatch.setPreferredSize(new java.awt.Dimension(653, 160));

        lbMatchResult.setText(" ");

        lbMatching.setText("Please select matching type and enter a matching expression:");

        cbMatchType.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "joker matching", "exact matching", "case insensitive", "similarity matching", "controlled matching", "merged expressions" }));
        cbMatchType.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMatchTypeActionPerformed(evt);
            }
        });

        cbMatchExpr.setEditable(true);
<<<<<<< HEAD
        cbMatchExpr.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "buy seven bananas", "buy bananas", "buy * bananas", "buy * *", "|EXACT|buy seven bananas", "buy 3 cookies", "|JOKER|buy * bananas", "give a bottle of wine", "give *", "give * * * *", "|JOKER|ICASE|give *", "buy enhanced lion shield", "Would you like to have an ice cream?", "Mary has a little lamb.", "I and you, he and they", "What is the an answer to life, the universe and everything?", "to be or not to be", "Take these three 烤排s and have fun!", "99 red balloons", "Hi, how are you?", "_Hi, how are you?" }));
=======
        cbMatchExpr.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "buy seven bananas", "buy bananas", "buy * bananas", "buy * *", "|EXACT|buy seven bananas", "buy 3 cookies", "|JOKER|buy * bananas", "give a bottle of 红酒", "give *", "give * * * *", "|JOKER|ICASE|give *", "buy enhanced lion shield", "Would you like to have an ice cream?", "Mary has a little lamb.", "I and you, he and they", "What is the an answer to life, the universe and everything?", "to be or not to be", "Take these three 烤排s and have fun!", "99 red balloons", "Hi, how are you?", "_Hi, how are you?" }));
>>>>>>> f76672e17df092a61ddb88a57859203a0a9ef0ae
        cbMatchExpr.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMatchExprActionPerformed(evt);
            }
        });

        lbParsedSentence.setText("parsed sentence:");

        tfParsedSentence.setEditable(false);

        lbParsedMatchString.setText("parsed match string:");

        tfParsedMatchString.setEditable(false);

        btMatch.setText("test match");
        btMatch.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btMatchActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panelMatchLayout = new org.jdesktop.layout.GroupLayout(panelMatch);
        panelMatch.setLayout(panelMatchLayout);
        panelMatchLayout.setHorizontalGroup(
            panelMatchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelMatchLayout.createSequentialGroup()
                .addContainerGap()
                .add(panelMatchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelMatchLayout.createSequentialGroup()
                        .add(cbMatchType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbMatchExpr, 0, 395, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(panelMatchLayout.createSequentialGroup()
                        .add(lbMatching)
                        .addContainerGap(209, Short.MAX_VALUE))
                    .add(panelMatchLayout.createSequentialGroup()
                        .add(btMatch)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lbMatchResult, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE))
                    .add(panelMatchLayout.createSequentialGroup()
                        .add(panelMatchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lbParsedMatchString)
                            .add(lbParsedSentence))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(panelMatchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(tfParsedMatchString, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                            .add(tfParsedSentence, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        panelMatchLayout.setVerticalGroup(
            panelMatchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelMatchLayout.createSequentialGroup()
                .add(lbMatching)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelMatchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbMatchType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cbMatchExpr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelMatchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfParsedSentence, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbParsedSentence))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelMatchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfParsedMatchString, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbParsedMatchString))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                .add(panelMatchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btMatch)
                    .add(lbMatchResult))
                .add(135, 135, 135))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(panelMatch, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btLoadEntitiesActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLoadEntitiesActionPerformed
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// initialise DefaultEntityManager to load item names
		SingletonRepository.getEntityManager();

		// update word count display
		updateWordCount();

		btLoadEntities.setEnabled(false);

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_btLoadEntitiesActionPerformed

    private void btLoadWorldActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLoadWorldActionPerformed
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
	    	// initialise TransactionPool
			new DatabaseFactory().initializeDatabase();

			try {
				// load zone configurations to register creature names
				new ZoneGroupsXMLLoader(new URI("/data/conf/zones.xml")).load();
			} catch (Exception e) {
				logger.warn("unable to load zones", e);
			}

			// Initialise quests
			SingletonRepository.getStendhalQuestSystem().init();

			// update word count display
			updateWordCount();

			btLoadEntities.setEnabled(false);
			btLoadWorld.setEnabled(false);
		} catch(Exception e) {
			String msg = "Exception: " + e.getMessage();
			JOptionPane.showMessageDialog(this, msg);
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
    }//GEN-LAST:event_btLoadWorldActionPerformed

    private void btWriteWordlistActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btWriteWordlistActionPerformed
		String msg;

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			msg = WordListUpdate.updateWordList(WordList.getInstance());
		} catch(Exception e) {
			msg = "Exception: " + e.getMessage();
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		JOptionPane.showMessageDialog(this, msg);
    }//GEN-LAST:event_btWriteWordlistActionPerformed

	private void btParseActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btParseActionPerformed
		updateParsed();
	}// GEN-LAST:event_btParseActionPerformed

	private void cbSentenceActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbSentenceActionPerformed
//		updateParsed();
		updateMatching();
	}// GEN-LAST:event_cbSentenceActionPerformed

	private String updateParsed() {
		String text = "";
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			text = cbSentence.getSelectedItem().toString();
			processSentence(text);
		} catch(Exception e) {
			String msg = "Exception: " + e.getMessage();
			JOptionPane.showMessageDialog(this, msg);
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		return text;
	}

	private void cbMatchExprActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbMatchExprActionPerformed
		updateMatching();
	}// GEN-LAST:event_cbMatchExprActionPerformed

	private void btMatchActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btMatchActionPerformed
		updateMatching();
	}// GEN-LAST:event_btMatchActionPerformed

	private void cbMatchTypeActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbMatchTypeActionPerformed
		updateMatching();
	}// GEN-LAST:event_cbMatchTypeActionPerformed

	private void updateMatching() {
		Object sel = cbMatchType.getSelectedItem();

		ExpressionMatcher selectedMatcher = null;
		boolean mergeExpressions = false;

		if (sel.equals("joker matching")) {
			selectedMatcher = new JokerExprMatcher();
		} else if (sel.equals("exact matching")) {
			selectedMatcher = new ExactExprMatcher();
		} else if (sel.equals("case insensitive")) {
			selectedMatcher = new CaseInsensitiveExprMatcher();
		} else if (sel.equals("similarity matching")) {
			selectedMatcher = new SimilarExprMatcher();
		} else if (sel.equals("controlled matching")) {
			//selectedMatcher = null;
		} else if (sel.equals("merged expressions")) {
			selectedMatcher = new ExpressionMatcher();
			mergeExpressions = true;
//		} else if (sel.equals("type expression")) {
//			selectedMatcher = new TypeExprMatcher();
		}

		String text = updateParsed();

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			String matchSel = cbMatchExpr.getSelectedItem().toString();
			processMatching(text, matchSel, selectedMatcher, mergeExpressions);
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new TestEnvDlg().setVisible(true);
			}
		});
	}

    private void updateWordCount() {
    	tfWordCount.setText(Integer.toString(WordList.getInstance().getWordCount()));
	}

	private static final String initialHtml = "<html><head>"
			+ "<title>An example HTMLDocument</title>"
			// + "<style type=\"text/css\"> ul {color: red;}</style>"
			+ "</head>" + "<body><p></p></body></html>";

	private HTMLDocument initHtml(JTextPane p) {
		p.setContentType("text/html");
		p.setText(initialHtml);

		return (HTMLDocument) tpExpressions.getDocument();
	}

	private void addHtml(HTMLDocument d, String html) {
		try {
			d.insertBeforeEnd(d.getDefaultRootElement(), html);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String decodeExprType(ExpressionType t) {
		StringBuilder type = new StringBuilder();

		if (t.isEmpty()) {
			type.append(" - UNKNOWN");
		} else {
			if (t.isSubject()) {
				type.append(" - subject");
			}
			if (t.isObject()) {
				type.append(" - object");
			}
			if (t.isName()) {
				type.append(" - name");
			}
			if (t.isAnimal()) {
				type.append(" - animal");
			}
			if (t.isFood()) {
				type.append(" - food");
			}
			if (t.isFluid()) {
				type.append(" - fluid");
			}
			if (t.isVerb()) {
				type.append(" - verb");
			}
			if (t.isGerund()) {
				type.append(" - gerund");
			}
			if (t.isNumeral()) {
				type.append(" - numeral");
			}
			if (t.isAdjective()) {
				type.append(" - adjective");
			}
			if (t.isPlural()) {
				type.append(" - plural");
			}
			if (t.isIgnore()) {
				type.append(" - ignore");
			}
			if (t.isPreposition()) {
				type.append(" - preposition");
			}
			if (t.isPronoun()) {
				type.append(" - pronoun");
			}
			if (t.hasQuestion()) {
				type.append(" - question word");
			}
			if (t.isObsessional()) {
				type.append(" - obsessional");
			}
			if (t.hasColor()) {
				type.append(" - color");
			}
			if (t.isConditional()) {
				type.append(" - conditional");
			}
			if (t.isNegated()) {
				type.append(" - negated");
			}
			if (t.isDynamic()) {
				type.append(" - DYNAMIC");
			}
		}

		return type.toString();
	}

	private void processSentence(String text) {
		Sentence sentence = ConversationParser.parse(text);

		tfTrimmed.setText(sentence.getTrimmedText());

		tfNormalized.setText(sentence.getNormalized());

		tfToString.setText(sentence.toString());

		tfTrigger.setText(sentence.getTriggerExpression().toString());

		String subj = sentence.getSubjectName();
		tfSubject.setText(subj != null ? subj :
			"[" + sentence.getSubjectCount() + " subjects]");

		String verb = sentence.getVerbString();
		tfVerb.setText(verb != null ? verb :
			"[" + sentence.getVerbCount() + " verbs]");

		Expression num = sentence.getNumeral();
		tfNumeral.setText(num != null ? num.getNormalized() :
			"[" + sentence.getNumeralCount() + " numerals]");

		String obj = sentence.getObjectName();
		tfObject.setText(obj != null ? obj :
			"[" + sentence.getObjectCount() + " objects]");

		HTMLDocument d = initHtml(tpExpressions);

		int unknown = 0;
		for (Expression e : sentence) {
			String style = "";
			ExpressionType t = e.getType();

			if (t.isEmpty()) {
				style = " style=\"color:red\"";
				++unknown;
			}

			String type = decodeExprType(t);

			addHtml(d, "<div" + style + ">" + e.getOriginal() + type + "</div>");
		}

		tpExpressions.setDocument(d);

		lbUnknownWarning.setVisible(unknown > 0);
	}

	private void processMatching(String text, String matchText, ExpressionMatcher matcher, boolean mergeExpressions) {
		Sentence matchSentence, sentence;

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (mergeExpressions) {
			// standard parsing including expression merging
			matchSentence = ConversationParser.parse(matchText);
		} else if (matcher == null) {
			// simple parsing for matching
			matchSentence = ConversationParser.parseAsMatcher(matchText);
			// detect matching mode in "controlled matching" mode from the given matching text
			matcher = matchSentence.getMatcher();
		} else {
			// parse with the given matcher object
			matchSentence = ConversationParser.parse(matchText, matcher);
		}

		tfParsedMatchString.setText(matchSentence.toString());

		if (mergeExpressions) {
			// standard parsing including expression merging
			sentence = ConversationParser.parse(text);
		} else if (matcher == null) {
			// simple parsing for matching
			sentence = ConversationParser.parseAsMatchingSource(text);
		} else {
			// parse with the given matcher object
			sentence = ConversationParser.parse(text, new ConvCtxForMatchingSource(), matcher);
		}

		tfParsedSentence.setText(sentence.toString());

		boolean matches = sentence.matchesFull(matchSentence);
		lbMatchResult.setText(matches? "-> The user input MATCHES the match expression!":
						"-> User input and match expression don't match.");

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}
