import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class Anreicherung extends JFrame {
	private static final long serialVersionUID = 7993160239917202433L;

	JTextField statusTextField;
	JButton selectSipButton;
	
	public Anreicherung() {
		this.getContentPane().setLayout(null);
		
		this.initWindow();
		
		this.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
	}
	
	protected void initWindow() {
		final JFileChooser sipChooser = new JFileChooser();		
		sipChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		sipChooser.showOpenDialog(this);
		File sipFolder = sipChooser.getSelectedFile();
		
		statusTextField = new JTextField("Wähle eine SIP von Festplatte...");
		statusTextField.setBounds(5, 10, 400, 25);
		this.getContentPane().add(statusTextField);
		
		selectSipButton = new JButton("Wähle...");
		selectSipButton.setBounds(5, 150, 30, 25);
//		selectSipButton.action
		this.getContentPane().add(selectSipButton);
		
		this.pack();
	}
	
	public void actionPerformed(ActionEvent e) {
		
	}

	public static void main(String[] args) {
		Anreicherung fenster = new Anreicherung();
		fenster.setBounds(10, 10, 420, 500);
		fenster.setVisible(true);
	}

}
