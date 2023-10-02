package de.hechler.patrick.codingame.tictactoe.training;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import de.hechler.patrick.codingame.tictactoe.training.env.Player;
import de.hechler.patrick.codingame.tictactoe.training.env.TTTField;
import de.hechler.patrick.codingame.tictactoe.training.env.TTTPos;

@SuppressWarnings("javadoc")
public class GUIPlayer implements Player.InitilizedPlayer {
	
	private final TTTField field;
	private final String   name;
	
	private SubField[][] panels;
	
	private final TTTPos turn = new TTTPos();
	private JFrame       frame;
	
	public GUIPlayer(TTTField field, String name) {
		this.field = field;
		this.name  = name;
	}
	
	private class SubField extends JPanel {
		
		private static final long serialVersionUID = -8128547485594228671L;
		
		private final int ox;
		private final int oy;
		
		public SubField(int x, int y) {
			this.ox = x;
			this.oy = y;
		}
		
		public void reload(boolean enabled) {
			for (int cnt = this.getComponentCount(); --cnt >= 0;) {
				this.remove(cnt);
			}
			int w = this.getWidth();
			int h = this.getWidth();
			this.setLocation(this.ox * w, this.oy * h);
			Object val = GUIPlayer.this.field.value(this.ox, this.oy);
			if (val instanceof int[] a) {
				int xo = Math.min((int) (w * 0.1d), 5);
				int yo = Math.min((int) (w * 0.1d), 5);
				w -= xo << 1;
				h -= yo << 1;
				w /= 3;
				h /= 3;
				if (w == 0) {
					Dimension s = init(0, new JButton()).getPreferredSize();
					w = s.width;
					h = s.height;
					if (w > h) {
						h = w;
					} else {
						w = h;
					}
					xo = 5;
					yo = 5;
					this.setSize(3 * w + 10, 3 * h + 10);
				}
				for (int i = 0; i < 9; i++) {
					int       v = a[i];
					final int x = i / 3;
					final int y = i % 3;
					JButton   b = init(v, new JButton());
					b.setLocation(xo + x * w, yo + y * h);
					b.setSize(w, h);
					this.add(b);
					if (enabled && v == 0) {
						b.addActionListener(e -> {
							TTTPos t = GUIPlayer.this.turn;
							synchronized (t) {
								t.innerY = y;
								t.innerX = x;
								t.outerY = this.oy;
								t.outerX = this.ox;
								t.notifyAll();
							}
							reload(false);
						});
					} else {
						b.setEnabled(false);
					}
				}
			} else {
				int    i = ((Integer) val).intValue();
				JLabel l = init(i, new JLabel());
				l.setLocation(0, 0);
				if (w == 0) {
					l.setSize(w, h);
				} else {
					Dimension s = l.getPreferredSize();
					this.setSize(s);
					l.setSize(s);
				}
				Font f = l.getFont();
				if (f != null) {
					l.setFont(f.deriveFont(32f));
				}
				this.add(l);
			}
		}
		
		private <C extends JComponent> C init(int val, C c) {
			switch (val) {
			case -1 -> {
				if (c instanceof JButton b) {
					b.setText("O");
					c.setBackground(Color.RED);
				} else {
					((JLabel) c).setText("O");
					c.setForeground(Color.RED);
				}
			}
			case 0 -> {
				if (c instanceof JButton b) {
					b.setText("-");
				} else {
					((JLabel) c).setText("-");
				}
			}
			case 1 -> {
				if (c instanceof JButton b) {
					b.setText("X");
					c.setBackground(Color.BLUE);
				} else {
					((JLabel) c).setText("X");
					c.setForeground(Color.BLUE);
				}
			}
			default -> throw new AssertionError("invalid value");
			}
			return c;
		}
		
	}
	
	public GUIPlayer load() {
		this.frame = new JFrame(this.name);
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JPanel p = new JPanel();
		p.setLayout(null);
		this.frame.setLayout(null);
		this.frame.setContentPane(p);
		this.panels = new SubField[3][3];
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				this.panels[x][y] = new SubField(x, y);
				this.panels[x][y].setLayout(null);
				p.add(this.panels[x][y]);
			}
		}
		this.panels[0][0].reload(false);
		Dimension size = this.panels[0][0].getSize();
		if (size.height > size.width) {
			size.width = size.height;
		} else {
			size.height = size.width;
		}
		size.height *= 5;
		size.width  *= 5;
		int w = size.width / 3;
		int h = size.height / 3;
		this.frame.setVisible(true);
		Insets i = this.frame.getInsets();
		size.height += i.top + i.bottom;
		size.width  += i.left + i.right;
		this.frame.setSize(size);
		this.frame.setVisible(false);
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				this.panels[x][y].setLocation(x * w + i.left, y * h + i.top);
				this.panels[x][y].setSize(w, h);
				this.panels[x][y].reload(false);
			}
		}
		this.frame.setLocationByPlatform(true);
		this.frame.setVisible(true);
		return this;
	}
	
	@Override
	public TTTPos doTurn(int outerX, int outerY, TTTPos lastEnemyTurn) {
		try {
			TTTPos t = this.turn;
			synchronized (t) {
				SwingUtilities.invokeAndWait(() -> {
					for (int x = 0; x < 3; x++) {
						for (int y = 0; y < 3; y++) {
							this.panels[x][y].reload(outerX != -1 ? x == outerX && y == outerY : true);
						}
					}
					this.frame.toFront();
					this.frame.requestFocus();
					this.frame.repaint();
				});
				t.wait();
				if (lastEnemyTurn == null) {
					lastEnemyTurn = new TTTPos();
				} // do not let the turn used for synchronizing escape
				lastEnemyTurn.innerX = t.innerX;
				lastEnemyTurn.innerY = t.innerY;
				lastEnemyTurn.outerX = t.outerX;
				lastEnemyTurn.outerY = t.outerY;
			}
			System.out.println("[" + this.name + "]: turn: " + lastEnemyTurn);
			return lastEnemyTurn;
		} catch (InvocationTargetException | InterruptedException e) {
			throw new AssertionError(e);
		}
	}
	
	@Override
	public void finish(int won) {
		switch (won) {
		case 2 -> JOptionPane.showMessageDialog(null, "the enemy made an invalid turn", "game finish", JOptionPane.INFORMATION_MESSAGE);
		case 1 -> JOptionPane.showMessageDialog(null, "you won", "game finish", JOptionPane.INFORMATION_MESSAGE);
		case 0 -> JOptionPane.showMessageDialog(null, "there is no winner", "game finish", JOptionPane.INFORMATION_MESSAGE);
		case -1 -> JOptionPane.showMessageDialog(null, "the enemy won", "game finish", JOptionPane.INFORMATION_MESSAGE);
		case -2 -> JOptionPane.showMessageDialog(null, "you made an invalid turn", "game finish", JOptionPane.INFORMATION_MESSAGE);
		}
		this.frame.dispose();
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
