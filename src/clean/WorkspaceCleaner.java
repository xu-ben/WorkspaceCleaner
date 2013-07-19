/*
 * 类名：		WorkspaceCleaner
 * 创建日期：	2012/06/04
 * 最近修改：	2013/07/19
 * 作者：		徐犇
 */

package clean;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * (编程)工作区清理类 
 * @author ben
 */
@SuppressWarnings("serial")
public final class WorkspaceCleaner extends JFrame implements ActionListener {
	/**
	 * 清理的文件夹数目
	 */
	public static int dirnum = 0;
	
	/**
	 * 清理的文件数目
	 */
	public static int filenum = 0;

	private JMenuItem menuCleanEclipseForCpp = new JMenuItem(
			"清理Eclipse(C++)");

	private JMenuItem menuCleanEclipseForJava = new JMenuItem(
			"清理Eclipse(Java)");

	private JMenuItem menuCleanVS2010 = new JMenuItem("清理VS2010");
	
	private JMenuItem menuCleanObj = new JMenuItem("清理Obj文件");

	private JMenuItem menuCleanDefaultDir = new JMenuItem("清理默认文件夹");

	WorkspaceCleaner() {

		JMenu menuOperate = new JMenu("操作(O)");
		menuOperate.setMnemonic('O');
		menuOperate.add(menuCleanEclipseForCpp);
		menuCleanEclipseForCpp.addActionListener(this);
		menuOperate.add(menuCleanEclipseForJava);
		menuCleanEclipseForJava.addActionListener(this);
		menuOperate.add(menuCleanVS2010);
		menuCleanVS2010.addActionListener(this);
		menuOperate.add(menuCleanObj);
		menuCleanObj.addActionListener(this);
		menuOperate.add(menuCleanDefaultDir);
		menuCleanDefaultDir.addActionListener(this);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuOperate);
		this.setJMenuBar(menuBar);

		// Container con = this.getContentPane();
		/**
		 * 使程序运行时在屏幕居中显示
		 */
		final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		final int width = 500;
		final int height = 309;
		final int left = (screen.width - width) / 2;
		final int top = (screen.height - height) / 2;
		this.setLocation(left, top);
		this.setSize(width, height);
		this.setTitle("清理工程临时文件");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new WorkspaceCleaner();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String filePath = null;
		JMenuItem m = (JMenuItem) e.getSource();
		int type = CleanProject.DEFAULT;
		if (m == menuCleanDefaultDir) {
			filePath = "H:\\ben\\workspace";
		} else {
			try {
				JFileChooser fileChooser = new JFileChooser(".");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int n = fileChooser.showOpenDialog(this);
				if (JFileChooser.APPROVE_OPTION == n) {
					filePath = fileChooser.getSelectedFile().getPath();
				} else {
					return;
				}
			} catch (Exception ex) {
				return;
			}
			if (m == menuCleanEclipseForCpp) {
				type = CleanProject.ECLIPSE_CPP;
			} else if (m == menuCleanEclipseForJava) {
				type = CleanProject.ECLIPSE_JAVA;
			} else if (m == menuCleanVS2010) {
				type = CleanProject.VS2010;
			}else if(m == menuCleanObj) {
				type = CleanProject.OBJ;
			}
		}

		if (filePath == null) {
			return;
		}
		CleanProject ce = new CleanProject();
		if (ce.run(filePath, type)) {
			JOptionPane.showMessageDialog(this, "清理完毕!\n共清理文件夹" + dirnum
					+ "个，文件" + filenum + "个!", "恭喜", JOptionPane.OK_OPTION);
		} else {
			JOptionPane.showMessageDialog(this, "清理出错!", "抱歉",
					JOptionPane.ERROR_MESSAGE);
		}

	}
}

class CleanProject {
	public static final int DEFAULT = 0;
	public static final int ECLIPSE_CPP = 1;
	public static final int ECLIPSE_JAVA = 2;
	public static final int VS2010 = 3;
	public static final int VS2008 = 4;
	public static final int VS98 = 5;
	public static final int OBJ = 6;

	/**
	 * 运行清理功能
	 * 
	 * @param filePath
	 * @return
	 */
	public boolean run(String filePath, int type) {
		File f = new File(filePath);
		try {
			clean(f, type);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean judgeName(String filename, int type) {
		switch (type) {
		case DEFAULT:
			return filename.equals("Debug") || filename.equals("Release")
					|| filename.equals("bin");
		case ECLIPSE_CPP:
			return filename.equals("Debug") || filename.equals("Release");
		case ECLIPSE_JAVA:
			return filename.equals("bin");
		case VS2008:
			return false;
		case VS2010:
			return filename.equals("Debug") || filename.equals("Release")
					|| filename.equals("ipch");
		case VS98:
			return false;
		default:
			return false;
		}
	}

	/**
	 * 对f指定的目录进行清理
	 * 
	 * @param f
	 */
	private void clean(File f, int type) {
		if (f.isDirectory()) {
			String name = f.getName();
			if(judgeName(name, type)) {
				delete(f);
				WorkspaceCleaner.dirnum++;
				return;
			}

			String[] list = f.list();
			if (list == null) {
				return;
			}
			for (int i = 0; i < list.length; i++) {
				File fchild = new File(f.getPath() + "\\\\" + list[i]);
				clean(fchild, type);
			}
		}else if(type == OBJ) {
			String name = f.getName();
			if(name.endsWith(".obj") || name.endsWith(".OBJ")) {
				f.delete();
				WorkspaceCleaner.filenum++;
			}
		}else if(type == VS2010) {
			String name = f.getName();
			if(name.endsWith(".sdf") || name.endsWith(".SDF")) {
				f.delete();
				WorkspaceCleaner.filenum++;
			}
		}
	}

	/**
	 * 对一个文件或目录进行递归删除
	 * 
	 * @param f
	 */
	private void delete(File f) {
		if (!f.isDirectory()) {
			f.delete();
			WorkspaceCleaner.filenum++;
			return;
		}
		String[] list = f.list();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File fchild = new File(f.getPath() + "\\\\" + list[i]);
				delete(fchild);
			}
		}
		f.delete();
		return;
	}
}
