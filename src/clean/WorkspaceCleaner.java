/*
 * 类名：		WorkspaceCleaner
 * 创建日期：	2012/06/04
 * 最近修改：	2019/05/29
 * 作者：		徐犇
 */

package clean;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * (编程)工作区清理类
 * 
 * @author ben
 */
public final class WorkspaceCleaner extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4889377108968286233L;

	/**
	 * 清理的文件夹数目
	 */
	public static int dirnum = 0;

	/**
	 * 清理的文件数目
	 */
	public static int filenum = 0;

	private JMenuItem menuCleanEclipseForCpp = new JMenuItem("清理Eclipse(C/C++)");

	private JMenuItem menuCleanEclipseForJava = new JMenuItem("清理Eclipse(Java)");

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
		CleanType type = CleanType.DEFAULT;
		if (m == menuCleanDefaultDir) {
			filePath = "E:\\workspace";
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
				type = CleanType.ECLIPSE_CPP;
			} else if (m == menuCleanEclipseForJava) {
				type = CleanType.ECLIPSE_JAVA;
			} else if (m == menuCleanVS2010) {
				type = CleanType.VS2010;
			} else if (m == menuCleanObj) {
				type = CleanType.OBJ;
			}
		}

		if (filePath == null) {
			return;
		}
		CleanProject ce = new CleanProject();
		if (ce.run(filePath, type)) {
			JOptionPane.showMessageDialog(this, "清理完毕!\n共清理文件夹" + dirnum
					+ "个，文件" + filenum + "个!", "恭喜", JOptionPane.OK_OPTION);
			dirnum = filenum = 0;
		} else {
			JOptionPane.showMessageDialog(this, "清理出错!", "抱歉",
					JOptionPane.ERROR_MESSAGE);
		}

	}
}

enum CleanType {
	DEFAULT, ECLIPSE_CPP, ECLIPSE_JAVA, VS2010, VS2008, VS98, OBJ
}

class CleanProject {

	/**
	 * 运行清理功能
	 * 
	 * @param filePath
	 * @return
	 */
	public boolean run(String filePath, CleanType type) {
		File f = new File(filePath);
		try {
			clean(f, type);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean judgeName(String filename, CleanType type) {
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
	private void clean(File f, CleanType type) {
		if (f.isDirectory()) {
			String name = f.getName();
			if (judgeName(name, type)) {
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
		} else if (type == CleanType.OBJ) {
			String name = f.getName();
			if (name.endsWith(".obj") || name.endsWith(".OBJ")) {
				f.delete();
				WorkspaceCleaner.filenum++;
			}
		} else if (type == CleanType.VS2010) {
			String name = f.getName();
			if (name.endsWith(".sdf") || name.endsWith(".SDF")) {
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
		if (f.isFile()) {
			System.out.println(f.getAbsolutePath());
			f.delete();
			WorkspaceCleaner.filenum++;
		} else if (f.isDirectory()) {
			String[] childList = f.list();
			if (childList == null) {
				return;
			}
			for (String child : childList) {
				File fchild = new File(f.getPath() + "\\\\" + child);
				delete(fchild);
			}
			System.out.println(f.getAbsolutePath());
			f.delete();
		}
	}
}
