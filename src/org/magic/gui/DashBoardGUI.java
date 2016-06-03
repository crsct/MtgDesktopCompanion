package org.magic.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.TableRowSorter;

import org.magic.api.beans.MagicEdition;
import org.magic.api.interfaces.abstracts.AbstractDashBoard;
import org.magic.gui.models.CardsShakerTableModel;
import org.magic.gui.models.EditionsShakerTableModel;
import org.magic.gui.renderer.CardShakeRenderer;
import org.magic.tools.MagicFactory;

import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DashBoardGUI extends JPanel {
	private JTable tableEdition;
	private JTable tableStandard;
	private JTable tableModern;
	private JTable tableLegacy;
	private JTable tableVintage;
    private JLabel lblDashBoardInfo;
	private CardsShakerTableModel modStandard;
	private CardsShakerTableModel modModern;
	private CardsShakerTableModel modLegacy;
	private CardsShakerTableModel modVintage;
	private EditionsShakerTableModel modEdition;
	
	private JComboBox cboEdition;
	
  
	public DashBoardGUI() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		List<MagicEdition> eds= new ArrayList<>();
		try {
			eds=MagicFactory.getInstance().getEnabledProviders().get(0).searchSetByCriteria(null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JPanel panneauBas = new JPanel();
		add(panneauBas, BorderLayout.SOUTH);
		
		lblDashBoardInfo = new JLabel("");
		panneauBas.add(lblDashBoardInfo);
		
		
		
		modLegacy= new CardsShakerTableModel();
		modModern = new CardsShakerTableModel();
		modStandard= new CardsShakerTableModel();
		modVintage = new CardsShakerTableModel();
		modEdition = new EditionsShakerTableModel();
		
		
		JPanel panneauShakers = new JPanel();
		tabbedPane.addTab("DashBoard", null, panneauShakers, null);
		panneauShakers.setLayout(new BorderLayout(0, 0));
		
		JPanel panneauFormat = new JPanel();
		panneauShakers.add(panneauFormat);
		panneauFormat.setLayout(new GridLayout(2, 2, 0, 0));
		
		JPanel panel_1 = new JPanel();
		panneauFormat.add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		tableStandard = new JTable();
		
		
		JScrollPane scrollPane_1 = new JScrollPane();
		
		panel_1.add(scrollPane_1);
		
		scrollPane_1.setViewportView(tableStandard);
		
		JLabel lblStandard = new JLabel("Standard");
		lblStandard.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblStandard.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblStandard, BorderLayout.NORTH);
		
		JPanel panel_2 = new JPanel();
		panneauFormat.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		tableLegacy = new JTable();
		
		
		JScrollPane scrollPane_2 = new JScrollPane();
		panel_2.add(scrollPane_2);
		
		scrollPane_2.setViewportView(tableLegacy);
		
		JLabel lblLegacy = new JLabel("Legacy");
		lblLegacy.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblLegacy.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(lblLegacy, BorderLayout.NORTH);
		
		JPanel panel_3 = new JPanel();
		panneauFormat.add(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		tableModern = new JTable();
		
		JScrollPane scrollPane_3 = new JScrollPane();
		panel_3.add(scrollPane_3, BorderLayout.CENTER);
		
		
		scrollPane_3.setViewportView(tableModern);
		
		JLabel lblModern = new JLabel("Modern");
		lblModern.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblModern.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(lblModern, BorderLayout.NORTH);
		
		JPanel panel_4 = new JPanel();
		panneauFormat.add(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));
		tableVintage = new JTable();
		
		JScrollPane scrollPane_4 = new JScrollPane();
		panel_4.add(scrollPane_4);
		
		scrollPane_4.setViewportView(tableVintage);
		
		JLabel lblVintage = new JLabel("Vintage");
		lblVintage.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblVintage.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblVintage, BorderLayout.NORTH);
		
		new TableFilterHeader(tableVintage, AutoChoices.ENABLED);
		new TableFilterHeader(tableModern, AutoChoices.ENABLED);
		new TableFilterHeader(tableStandard, AutoChoices.ENABLED);
		new TableFilterHeader(tableLegacy, AutoChoices.ENABLED);
		
		new TableFilterHeader(tableEdition, AutoChoices.ENABLED);
		
		
		JPanel panneauHaut = new JPanel();
		panneauShakers.add(panneauHaut, BorderLayout.NORTH);
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				update();
			}

		});
		panneauHaut.add(btnRefresh);
		
		JPanel panneauEdition = new JPanel();
		tabbedPane.addTab("Editions", null, panneauEdition, null);
		panneauEdition.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panneauEdition.add(panel, BorderLayout.NORTH);
		cboEdition = new JComboBox(eds.toArray());
		cboEdition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				MagicEdition ed = (MagicEdition)cboEdition.getSelectedItem();
				modEdition.init(ed);
				modEdition.fireTableDataChanged();
				tableEdition.setRowSorter(new TableRowSorter(modEdition) );
			}
		});
		panel.add(cboEdition);
		
		JScrollPane scrollPane = new JScrollPane();
		panneauEdition.add(scrollPane, BorderLayout.CENTER);
		
		tableEdition = new JTable(modEdition);
		
		scrollPane.setViewportView(tableEdition);
		
		update();
	}

	private void update() {
			new Thread(new Runnable() {
			
			@Override
			public void run() {
			
				modStandard.init(AbstractDashBoard.FORMAT.standard);
				tableStandard.setModel(modStandard);
				tableStandard.setRowSorter(new TableRowSorter(modStandard) );
				modStandard.fireTableDataChanged();

				
				modLegacy.init(AbstractDashBoard.FORMAT.legacy);
				tableLegacy.setModel(modLegacy);
				tableLegacy.setRowSorter(new TableRowSorter(modLegacy) );
				modLegacy.fireTableDataChanged();
			
				
				modModern.init(AbstractDashBoard.FORMAT.modern);
				tableModern.setModel(modModern);
				tableModern.setRowSorter(new TableRowSorter(modModern) );
				modModern.fireTableDataChanged();
				
				
				
				modVintage.init(AbstractDashBoard.FORMAT.vintage);
				tableVintage.setModel(modVintage);
				tableVintage.setRowSorter(new TableRowSorter(modVintage) );
				modVintage.fireTableDataChanged();
				
				
				List<SortKey> keys = new ArrayList<SortKey>();
				SortKey sortKey = new SortKey(2, SortOrder.DESCENDING);//column index 2
				keys.add(sortKey);
				
				((TableRowSorter)tableVintage.getRowSorter()).setSortKeys(keys);
				((TableRowSorter)tableVintage.getRowSorter()).sort();
				
				((TableRowSorter)tableLegacy.getRowSorter()).setSortKeys(keys);
				((TableRowSorter)tableLegacy.getRowSorter()).sort();

				((TableRowSorter)tableStandard.getRowSorter()).setSortKeys(keys);
				((TableRowSorter)tableStandard.getRowSorter()).sort();

				((TableRowSorter)tableModern.getRowSorter()).setSortKeys(keys);
				((TableRowSorter)tableModern.getRowSorter()).sort();
		
				
				tableVintage.getColumnModel().getColumn(3).setCellRenderer(new CardShakeRenderer());
				tableModern.getColumnModel().getColumn(3).setCellRenderer(new CardShakeRenderer());
				tableStandard.getColumnModel().getColumn(3).setCellRenderer(new CardShakeRenderer());
				tableLegacy.getColumnModel().getColumn(3).setCellRenderer(new CardShakeRenderer());
				tableEdition.getColumnModel().getColumn(3).setCellRenderer(new CardShakeRenderer());
				tableEdition.getColumnModel().getColumn(5).setCellRenderer(new CardShakeRenderer());
			
				
				lblDashBoardInfo.setText(MagicFactory.getInstance().getEnabledDashBoard().getName() + "(updated : " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(MagicFactory.getInstance().getEnabledDashBoard().getUpdatedDate())+")");
				
			}
		},"Thread-UpdateDashBoard").start();
		
	}
}
