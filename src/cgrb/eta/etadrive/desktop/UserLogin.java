/* * Copyright 2012 Oregon State University.
 * All Rights Reserved. 
 *  
 * Permission to use, copy, modify, and distribute this software and its 
 * documentation for educational, research and non-profit purposes, without fee, 
 * and without a written agreement is hereby granted, provided that the above 
 * copyright notice, this paragraph and the following three paragraphs appear in 
 * all copies. 
 *
 * Permission to incorporate this software into commercial products may be 
 * obtained by contacting OREGON STATE UNIVERSITY Office for 
 * Commercialization and Corporate Development.
 *
 * This software program and documentation are copyrighted by OREGON STATE
 * UNIVERSITY. The software program and documentation are supplied "as is", 
 * without any accompanying services from the University. The University does 
 * not warrant that the operation of the program will be uninterrupted or errorfree. 
 * The end-user understands that the program was developed for research 
 * purposes and is advised not to rely exclusively on the program for any reason. 
 *
 * IN NO EVENT SHALL OREGON STATE UNIVERSITY BE LIABLE TO ANY PARTY 
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
 * DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS 
 * SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE OREGON STATE  
 * UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * OREGON STATE UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE AND ANY 
 * STATUTORY WARRANTY OF NON-INFRINGEMENT. THE SOFTWARE PROVIDED 
 * HEREUNDER IS ON AN "AS IS" BASIS, AND OREGON STATE UNIVERSITY HAS 
 * NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
 * ENHANCEMENTS, OR MODIFICATIONS. 
 * 
 */
package cgrb.eta.etadrive.desktop;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JPasswordField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserLogin extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1924507675555197958L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JPasswordField passwordField;
	private JButton okButton;

	public void showMe() {
		try {
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setVisible(true);
			setLocationRelativeTo(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UserLogin setActionListener(ActionListener listener) {
		okButton.addActionListener(listener);
		return this;
	}

	/**
	 * Create the dialog.
	 */
	public UserLogin() {
		setTitle("Login to ETA");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UserLogin.class.getResource("/cgrb/eta/client/images/favicon.png")));
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JPanel panel = new JPanel();
			panel.setBounds(29, 58, 395, 120);
			contentPanel.add(panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 99, 72 };
			gbl_panel.rowHeights = new int[] { 28, 13, 0, 0, 0, 0 };
			gbl_panel.columnWeights = new double[] { 0.0, 1.0 };
			gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gbl_panel);
			{
				JLabel lblNewLabel = new JLabel("User Name:");
				GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
				gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
				gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel.gridx = 0;
				gbc_lblNewLabel.gridy = 0;
				panel.add(lblNewLabel, gbc_lblNewLabel);
			}
			{
				textField = new JTextField();
				GridBagConstraints gbc_textField = new GridBagConstraints();
				gbc_textField.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField.anchor = GridBagConstraints.NORTH;
				gbc_textField.insets = new Insets(0, 0, 5, 0);
				gbc_textField.gridx = 1;
				gbc_textField.gridy = 0;
				panel.add(textField, gbc_textField);
				textField.setColumns(20);
			}
			{
				JLabel lblPassword = new JLabel("Password:");
				GridBagConstraints gbc_lblPassword = new GridBagConstraints();
				gbc_lblPassword.anchor = GridBagConstraints.EAST;
				gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
				gbc_lblPassword.gridx = 0;
				gbc_lblPassword.gridy = 1;
				panel.add(lblPassword, gbc_lblPassword);
			}
			{
				passwordField = new JPasswordField();
				GridBagConstraints gbc_passwordField = new GridBagConstraints();
				gbc_passwordField.insets = new Insets(0, 0, 5, 0);
				gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
				gbc_passwordField.gridx = 1;
				gbc_passwordField.gridy = 1;
				panel.add(passwordField, gbc_passwordField);
			}
			{
				JLabel lblNewLabel_1 = new JLabel("Note: Your username is your login name + @ and the url of ETA");
				GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
				gbc_lblNewLabel_1.gridwidth = 2;
				gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
				gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
				gbc_lblNewLabel_1.gridx = 0;
				gbc_lblNewLabel_1.gridy = 2;
				panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
				lblNewLabel_1.setFont(new Font("DejaVu Sans Condensed", Font.PLAIN, 10));
				lblNewLabel_1.setVerticalAlignment(SwingConstants.TOP);
				lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
			}
			{
				JLabel lblMyLoginWould = new JLabel("my login would be alex@eta.webserver.com");
				GridBagConstraints gbc_lblMyLoginWould = new GridBagConstraints();
				gbc_lblMyLoginWould.anchor = GridBagConstraints.NORTHWEST;
				gbc_lblMyLoginWould.gridwidth = 2;
				gbc_lblMyLoginWould.gridx = 0;
				gbc_lblMyLoginWould.gridy = 4;
				panel.add(lblMyLoginWould, gbc_lblMyLoginWould);
				lblMyLoginWould.setVerticalAlignment(SwingConstants.TOP);
				lblMyLoginWould.setHorizontalAlignment(SwingConstants.LEFT);
				lblMyLoginWould.setFont(new Font("DejaVu Serif Condensed", Font.PLAIN, 10));
			}
			{
				JLabel lblNewLabel_2 = new JLabel("Ex: login name of alex and the ETA web page is eta.webserver.com");
				GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
				gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
				gbc_lblNewLabel_2.anchor = GridBagConstraints.NORTHWEST;
				gbc_lblNewLabel_2.gridwidth = 2;
				gbc_lblNewLabel_2.gridx = 0;
				gbc_lblNewLabel_2.gridy = 3;
				panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
				lblNewLabel_2.setVerticalAlignment(SwingConstants.TOP);
				lblNewLabel_2.setFont(new Font("DejaVu Sans Condensed", Font.PLAIN, 10));
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						System.exit(1);
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}

	public String getUsername() {
		return textField.getText();
	}

	public String getPassword() {
		return new String(passwordField.getPassword());
	}
}
