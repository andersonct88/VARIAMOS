package com.variamos.gui.maineditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.cfm.common.AbstractModel;
import com.cfm.productline.AbstractElement;
import com.cfm.productline.Editable;
import com.cfm.productline.ProductLine;
import com.cfm.productline.io.SXFMReader;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxCell;
import com.mxgraph.shape.mxStencilShape;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel;
import com.variamos.gui.pl.editor.ConfigurationPropertiesTab;
import com.variamos.gui.pl.editor.ConfiguratorPanel;
import com.variamos.gui.pl.editor.PLEditorToolBar;
import com.variamos.gui.pl.editor.PLGraphEditorFunctions;
import com.variamos.gui.pl.editor.ProductLineGraph;
import com.variamos.gui.pl.editor.SpringUtilities;
import com.variamos.gui.pl.editor.widgets.WidgetPL;
import com.variamos.gui.refas.editor.ModelButtonAction;
import com.variamos.gui.refas.editor.RefasEditorToolBar;
import com.variamos.gui.refas.editor.RefasGraph;
import com.variamos.gui.refas.editor.RefasGraphEditorFunctions;
import com.variamos.gui.refas.editor.SemanticPlusSyntax;
import com.variamos.gui.refas.editor.panels.ElementDesignPanel;
import com.variamos.gui.refas.editor.panels.RefasExpressionPanel;
import com.variamos.gui.refas.editor.widgets.MClassWidget;
import com.variamos.gui.refas.editor.widgets.MEnumerationWidget;
import com.variamos.gui.refas.editor.widgets.RefasWidgetFactory;
import com.variamos.gui.refas.editor.widgets.WidgetR;
import com.variamos.refas.core.refas.Refas;
import com.variamos.refas.core.simulationmodel.Refas2Hlcl;
import com.variamos.refas.core.types.PerspectiveType;
import com.variamos.syntaxsupport.metamodel.EditableElement;
import com.variamos.syntaxsupport.metamodel.InstAttribute;
import com.variamos.syntaxsupport.metamodel.InstConcept;
import com.variamos.syntaxsupport.metamodel.InstElement;
import com.variamos.syntaxsupport.metamodel.InstOverTwoRelation;
import com.variamos.syntaxsupport.metamodel.InstPairwiseRelation;
import com.variamos.syntaxsupport.metamodel.InstView;
import com.variamos.syntaxsupport.metamodelsupport.AbstractAttribute;
import com.variamos.syntaxsupport.metamodelsupport.EditableElementAttribute;
import com.variamos.syntaxsupport.metamodelsupport.MetaConcept;
import com.variamos.syntaxsupport.metamodelsupport.MetaElement;
import com.variamos.syntaxsupport.metamodelsupport.MetaView;
import com.variamos.syntaxsupport.metamodelsupport.SimulationConfigAttribute;
import com.variamos.syntaxsupport.metamodelsupport.SimulationStateAttribute;
import com.variamos.syntaxsupport.semanticinterface.IntSemanticElement;
import com.variamos.syntaxsupport.type.DomainRegister;

import fm.FeatureModelException;

/**
 * @author jcmunoz
 *
 */
/**
 * @author jcmunoz
 *
 */
@SuppressWarnings("serial")
public class VariamosGraphEditor extends BasicGraphEditor {

	static {
		try {
			mxResources.add("com/variamos/gui/maineditor/resources/editor");
		} catch (Exception e) {
			// ignore
		}
	}
	private int modelViewIndex = 0;
	private int modelSubViewIndex = 0;
	private List<String> validElements = null;

	private List<MetaView> metaViews = null;

	protected DomainRegister domainRegister = new DomainRegister();
	protected GraphTree productLineIndex;
	protected ConfiguratorPanel configurator;
	protected ConfigurationPropertiesTab configuratorProperties;

	protected RefasExpressionPanel expressions;
	protected JTextArea messagesArea;
	protected JTextArea expressionsArea;
	private ElementDesignPanel elementDesignPanel;
	protected JPanel elementConfigPropPanel;
	protected JPanel elementExpressionPanel;
	protected JPanel elementSimPropPanel;
	protected PerspectiveToolBar perspectiveToolBar;
	// Bottom tabs
	protected JTabbedPane extensionTabs;
	protected static SemanticPlusSyntax sematicSyntaxObject;

	protected int mode = 0;
	private int tabIndex = 0, lastTabIndex = 0;
	private Refas2Hlcl refas2hlcl;
	private VariamosGraphEditor modelEditor;
	private EditableElement lastEditableElement;
	private boolean recursiveCall = false;
	private boolean updateExpressions = true;
	private String editableElementType = null;

	public Refas2Hlcl getRefas2hlcl() {
		return refas2hlcl;
	}

	public VariamosGraphEditor getEditor() {
		return this;
	}

	public VariamosGraphEditor(String appTitle,
			VariamosGraphComponent component, int perspective,
			AbstractModel abstractModel) {
		super(appTitle, component, perspective);

		metaViews = sematicSyntaxObject.getMetaViews();
		refas2hlcl = new Refas2Hlcl((Refas) abstractModel);
		
		configurator.setRefas2hlcl(refas2hlcl);
		
		registerEvents();
		((AbstractGraph) graphComponent.getGraph()).setModel(abstractModel);
		if (perspective == 0) {
			setPerspective(0);
			graphEditorFunctions = new PLGraphEditorFunctions(this);
			graphEditorFunctions.updateEditor(validElements,
					getGraphComponent(), modelViewIndex);
		}
		mxCell root = new mxCell();
		root.insert(new mxCell());
		RefasGraph refasGraph = (RefasGraph) component.getGraph();
		refasGraph.getModel().setRoot(root);
		for (int i = 0; i < metaViews.size(); i++) {
			mxCell parent = new mxCell("mv" + i);
			refasGraph.addCell(parent);
			MetaView metaView = metaViews.get(i);
			JPanel tabPane = new JPanel();
			if (metaView.getChildViews().size() > 0) {
				modelsTabPane.add(metaView.getName(), tabPane);
				refasGraph.addCell(new mxCell("mv" + i), parent);
				// Add the parent as first child
				for (int j = 0; j < metaView.getChildViews().size(); j++) {
					refasGraph.addCell(new mxCell("mv" + i + "-" + j), parent);
					MetaView metaChildView = metaView.getChildViews().get(j);
					JButton a = new JButton(metaChildView.getName());
					tabPane.add(a);
					a.addActionListener(new ModelButtonAction());
				}
				// TODO include recursive calls if more view levels are required
			} else {
				modelsTabPane.add(metaView.getName(), tabPane);
			}
			final EditorPalette palette = new EditorPalette();
			palette.setName("ee");
			final JScrollPane scrollPane = new JScrollPane(palette);
			scrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			modelsTabPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					List<MetaView> metaViews = sematicSyntaxObject
							.getMetaViews();
					VariamosGraphEditor editor = getEditor();
					((MainFrame) editor.getFrame()).waitingCursor(true);
					int modelInd = getModelViewIndex();
					for (int i = 0; i < metaViews.size(); i++) {
						if (modelInd != i
								&& modelsTabPane.getTitleAt(
										modelsTabPane.getSelectedIndex())
										.equals(metaViews.get(i).getName())) {

							if (metaViews.get(i).getChildViews().size() > 0) {
								// if (false) //TODO validate the name of the
								// button with the tab, if true, identify the
								// subview
								// editor.setVisibleModel(i ,0);
								// else
								editor.setVisibleModel(i, 0);
								editor.updateView();
								center.setDividerLocation(60);
								center.setMaximumSize(center.getSize());
								center.setMinimumSize(center.getSize());
								center.setResizeWeight(0);
							} else {
								editor.setVisibleModel(i, -1);
								editor.updateView();
								center.setDividerLocation(25);
								center.setMaximumSize(center.getSize());
								center.setMinimumSize(center.getSize());
								center.setPreferredSize(center.getSize());
							}
						}
					}
					((MainFrame) editor.getFrame()).waitingCursor(false);
				}
			});
		}
		setModified(false);
	}

	public VariamosGraphEditor(MainFrame frame,
			VariamosGraphComponent component, int perspective,
			AbstractModel abstractModel) {
		super(frame, "", component, perspective);

		metaViews = new ArrayList<MetaView>();
		refas2hlcl = new Refas2Hlcl((Refas) abstractModel);
		
		configurator.setRefas2hlcl(refas2hlcl);
		
		
		registerEvents();
		Collection<InstView> instViews = ((Refas) abstractModel)
				.getSyntaxRefas().getInstViews();
		((AbstractGraph) graphComponent.getGraph()).setModel(abstractModel);
		graphEditorFunctions = new RefasGraphEditorFunctions(this);
		RefasGraph refasGraph = (RefasGraph) component.getGraph();

		this.graphLayout("organicLayout", false);
		this.getGraphComponent().zoomAndCenter();
		if (instViews.size() == 0) {
			center.setDividerLocation(0);
			upperPart.setDividerLocation(0);
			graphAndRight.setDividerLocation(700);
			setVisibleModel(-1, -1);

			updateView();
		} else {
			int i = 0;
			for (InstView instView : instViews) {
				mxCell parent = new mxCell("mv" + i);
				refasGraph.addCell(parent);
				MetaView metaView = (MetaView) instView
						.getEditableMetaElement();
				metaViews.add(metaView);
				JPanel tabPane = new JPanel();
				if (metaView.getChildViews().size() > 0) {
					modelsTabPane.add(metaView.getName(), tabPane);
					refasGraph.addCell(new mxCell("mv" + i), parent);
					// Add the parent as first child
					for (int j = 0; j < metaView.getChildViews().size(); j++) {
						refasGraph.addCell(new mxCell("mv" + i + "-" + j),
								parent);
						MetaView metaChildView = metaView.getChildViews()
								.get(j);
						JButton a = new JButton(metaChildView.getName());
						tabPane.add(a);
						a.addActionListener(new ModelButtonAction());
					}
					// TODO include recursive calls if more view levels are
					// required
				} else {
					modelsTabPane.add(metaView.getName(), tabPane);
					tabPane.setMaximumSize(new Dimension(0, 0));

				}
				final EditorPalette palette = new EditorPalette();
				palette.setName("ee");
				final JScrollPane scrollPane = new JScrollPane(palette);
				scrollPane
						.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane
						.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				modelsTabPane.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						// System.out.println("Tab: "
						// + modelsTabPane.getTitleAt(modelsTabPane
						// .getSelectedIndex()));
						List<MetaView> metaViews = sematicSyntaxObject
								.getMetaViews();
						VariamosGraphEditor editor = getEditor();
						((MainFrame) editor.getFrame()).waitingCursor(true);
						int modelInd = getModelViewIndex();
						for (int i = 0; i < metaViews.size(); i++) {
							if (modelInd != i
									&& modelsTabPane.getTitleAt(
											modelsTabPane.getSelectedIndex())
											.equals(metaViews.get(i).getName())) {

								if (metaViews.get(i).getChildViews().size() > 0) {
									// if (false) //TODO validate the name of
									// the
									// button with the tab, if true, identify
									// the
									// subview
									// editor.setVisibleModel(i ,0);
									// else
									editor.setVisibleModel(i, 0);
									editor.updateView();
									center.setDividerLocation(60);
									center.setMaximumSize(center.getSize());
									center.setMinimumSize(center.getSize());
									center.setResizeWeight(0);
								} else {
									editor.setVisibleModel(i, -1);
									editor.updateView();
									center.setDividerLocation(25);
									center.setMaximumSize(center.getSize());
									center.setMinimumSize(center.getSize());
									center.setPreferredSize(center.getSize());
								}
							}
						}
						((MainFrame) editor.getFrame()).waitingCursor(false);
					}
				});
				i++;
			}
			center.setDividerLocation(25);
			upperPart.setDividerLocation(0);
			if (((Refas) abstractModel).getPerspectiveType().equals(
					PerspectiveType.simulation))
				graphAndRight.setDividerLocation(1100);
			else
				graphAndRight.setDividerLocation(700);

			setVisibleModel(0, -1);
			updateView();
		}
	}

	public SemanticPlusSyntax getSematicSintaxObject() {
		return sematicSyntaxObject;
	}

	public AbstractGraphEditorFunctions getGraphEditorFunctions() {
		return graphEditorFunctions;
	}

	public void setGraphEditorFunctions(AbstractGraphEditorFunctions gef) {
		graphEditorFunctions = gef;
	}

	public int getModelViewIndex() {
		return modelViewIndex;
	}

	public int getModelSubViewIndex() {
		return modelSubViewIndex;
	}

	public List<MetaView> getMetaViews() {
		return metaViews;
	}

	public void setVisibleModel(int modelIndex, int modelSubIndex) {
		// System.out.println(modelIndex + " " + modelSubIndex);
		modelViewIndex = modelIndex;
		modelSubViewIndex = modelSubIndex;
		RefasGraph mode = ((RefasGraph) getGraphComponent().getGraph());
		validElements = mode
				.getValidElements(modelViewIndex, modelSubViewIndex);
		mode.setModelViewIndex(modelIndex);
		mode.setModelViewSubIndex(modelSubIndex);
		mode.showElements();

		elementDesignPanel.repaint();
		// elementDesPropPanel.repaint();
		elementConfigPropPanel.repaint();
		elementExpressionPanel.repaint();
		elementSimPropPanel.repaint();
	}

	public void updateEditor() {
		graphEditorFunctions.updateEditor(this.validElements,
				getGraphComponent(), modelViewIndex);
		perspectiveToolBar.updateButtons();
	}

	public void updateView() {
		graphEditorFunctions.updateView(this.validElements,
				getGraphComponent(), modelViewIndex);
		// perspectiveToolBar.updateButtons();
	}

	/**
	 * @param appTitle
	 * @param component
	 *            New constructor to load directly files and perspectives
	 * @throws FeatureModelException
	 */
	public static VariamosGraphEditor loader(String appTitle, String file,
			String perspective) throws FeatureModelException {
		AbstractModel abstractModel = null;
		sematicSyntaxObject = new SemanticPlusSyntax();

		int persp = 0;
		if (perspective.equals("ProductLine")) {
			persp = 0;
			if (file != null) {
				SXFMReader reader = new SXFMReader();
				abstractModel = reader.readFile(file);
			} else

				abstractModel = new ProductLine();
			ProductLineGraph plGraph = new ProductLineGraph();
			// plGraph.add
			VariamosGraphEditor vge = new VariamosGraphEditor(
					"Configurator - VariaMos", new VariamosGraphComponent(
							plGraph, Color.WHITE), persp, abstractModel);
			return vge;
		} else if (perspective.equals("modeling")) {

			System.out.println("Initializing modeling perspective...");
			persp = 2;
			RefasGraph refasGraph = null;
			if (file != null) {
				SXFMReader reader = new SXFMReader();
				abstractModel = reader.readRefasFile(file, new Refas(
						PerspectiveType.modeling));
				refasGraph = new RefasGraph(sematicSyntaxObject);
			} else {
				{
					abstractModel = new Refas(PerspectiveType.modeling);
					refasGraph = new RefasGraph(sematicSyntaxObject);

				}

				// ProductLineGraph plGraph2 = new ProductLineGraph();
				VariamosGraphEditor vge2 = new VariamosGraphEditor(
						"Configurator - VariaMos", new VariamosGraphComponent(
								refasGraph, Color.WHITE), persp, abstractModel);
				vge2.createFrame().setVisible(true);
				vge2.setVisibleModel(0, -1);
				vge2.setDefaultButton();
				vge2.setPerspective(2);
				vge2.setGraphEditorFunctions(new RefasGraphEditorFunctions(vge2));
				vge2.updateEditor();

				System.out.println("Modeling perspective initialized.");
				return vge2;
			}
		} else if (perspective.equals("metamodeling")) {

			System.out.println("Initializing meta-modeling perspective...");
			// todo: change for metamodeling
			persp = 3;
			RefasGraph refasGraph = null;
			if (file != null) {
				SXFMReader reader = new SXFMReader();
				abstractModel = reader.readRefasFile(file, new Refas(
						PerspectiveType.modeling));
				refasGraph = new RefasGraph(sematicSyntaxObject);
			} else {
				{
					abstractModel = new Refas(PerspectiveType.modeling);
					refasGraph = new RefasGraph(sematicSyntaxObject);

				}

				// ProductLineGraph plGraph2 = new ProductLineGraph();
				VariamosGraphEditor vge2 = new VariamosGraphEditor(
						"Configurator - VariaMos", new VariamosGraphComponent(
								refasGraph, Color.WHITE), persp, abstractModel);
				vge2.createFrame().setVisible(true);
				vge2.setVisibleModel(0, -1);
				vge2.setPerspective(3);
				vge2.setGraphEditorFunctions(new RefasGraphEditorFunctions(vge2));
				vge2.updateEditor();
				mxCell root = new mxCell();
				root.insert(new mxCell());
				refasGraph.getModel().setRoot(root);
				System.out.println("Meta-Modeling perspective initialized.");
				return vge2;
			}
		}
		return null;
	}

	public static SemanticPlusSyntax getSematicSyntaxObject() {
		return sematicSyntaxObject;
	}

	public static void setSematicSyntaxObject(
			SemanticPlusSyntax sematicSyntaxObject) {
		VariamosGraphEditor.sematicSyntaxObject = sematicSyntaxObject;
	}

	public void editModel(AbstractModel pl) {
		// productLineIndex.reset();
		AbstractGraph abstractGraph = null;

		// todo: review other perspectives
		if (perspective == 0 || perspective == 1)
			abstractGraph = new ProductLineGraph();
		if (perspective == 2 || perspective == 3 || perspective == 4)
			abstractGraph = new RefasGraph(sematicSyntaxObject);
		// abstractGraph = (AbstractGraph) getGraphComponent()
		// .getGraph();
		((VariamosGraphComponent) graphComponent).updateGraph(abstractGraph);
		registerEvents();

		abstractGraph.setModel(pl);

		// productLineIndex.populate(pl);

	}

	public void resetView() {
		updateEditor();
		mxGraph graph = getGraphComponent().getGraph();
		// Check modified flag and display save dialog
		mxCell root = new mxCell();
		root.insert(new mxCell());
		graph.getModel().setRoot(root);
		if (perspective == 2) {
			setGraphEditorFunctions(new RefasGraphEditorFunctions(this));

			((RefasGraph) graph).defineInitialGraph();
		}
		if (perspective % 2 != 0) {
			this.graphLayout("organicLayout", true);
			this.getGraphComponent().zoomAndCenter();
		}
		setCurrentFile(null);
		getGraphComponent().zoomAndCenter();

		setModified(false);
	}

	private void registerEvents() {
		mxGraphSelectionModel selModel = getGraphComponent().getGraph()
				.getSelectionModel();
		selModel.addListener(mxEvent.CHANGE, new mxIEventListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				// Collection<mxCell> added = (Collection<mxCell>)
				// evt.getProperty("added");
				// System.out.println("Added: " + added);

				Collection<mxCell> removed = (Collection<mxCell>) evt
						.getProperty("removed");
				// System.out.println("Removed: " + removed);

				// editProperties(null);
				editPropertiesRefas(null);

				if (removed == null)
					return;

				mxCell cell = null;
				if (removed.size() == 1)
					cell = removed.iterator().next();

				// Multiselection case
				if (cell == null) {

					return;
				}
				// if (cell.getValue() instanceof Editable) {
				// Editable elm = (Editable) cell.getValue();
				// editProperties(elm);
				// // getGraphComponent().scrollCellToVisible(cell, true);
				// }

				if (cell.getValue() instanceof EditableElement) {
					EditableElement elm = (EditableElement) cell.getValue();
					editPropertiesRefas(elm);
					// getGraphComponent().scrollCellToVisible(cell, true);
				}
			}
		});
	}

	public static String loadShape(EditorPalette palette, File f)
			throws IOException {
		String nodeXml = mxUtils.readFile(f.getAbsolutePath());
		addStencilShape(palette, nodeXml, f.getParent() + File.separator);
		return nodeXml;
	}

	/**
	 * Loads and registers the shape as a new shape in mxGraphics2DCanvas and
	 * adds a new entry to use that shape in the specified palette
	 * 
	 * @param palette
	 *            The palette to add the shape to.
	 * @param nodeXml
	 *            The raw XML of the shape
	 * @param path
	 *            The path to the directory the shape exists in
	 * @return the string name of the shape
	 */
	public static String addStencilShape(EditorPalette palette, String nodeXml,
			String path) {

		// Some editors place a 3 byte BOM at the start of files
		// Ensure the first char is a "<"
		int lessthanIndex = nodeXml.indexOf("<");
		nodeXml = nodeXml.substring(lessthanIndex);
		mxStencilShape newShape = new mxStencilShape(nodeXml);
		String name = newShape.getName();
		ImageIcon icon = null;

		if (path != null) {
			String iconPath = path + newShape.getIconPath();
			icon = new ImageIcon(iconPath);
		}

		// Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(name, newShape);

		if (palette != null && icon != null) {
			palette.addTemplate(name, icon, "shape=" + name, 80, 80, "");
		}

		return name;
	}

	@Override
	protected Component getLeftComponent() {
		productLineIndex = new GraphTree();
		productLineIndex.bind((AbstractGraph) getGraphComponent().getGraph());

		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				productLineIndex, null);
		inner.setDividerLocation(250);
		inner.setResizeWeight(1);
		inner.setDividerSize(6);
		inner.setBorder(null);

		return inner;
	}

	@Override
	public Component getExtensionsTab(final EditableElement elm) {
		if (extensionTabs != null)
			return extensionTabs;

		messagesArea = new JTextArea("Output");
		messagesArea.setEditable(false);

		// elementDesPropPanel = new JPanel();
		// elementDesPropPanel.setLayout(new SpringLayout());

		elementDesignPanel = new ElementDesignPanel();

		elementConfigPropPanel = new JPanel();
		elementConfigPropPanel.setLayout(new SpringLayout());

		elementExpressionPanel = new JPanel();
		// elementExpressionPanel.setLayout(new SpringLayout());

		expressionsArea = new JTextArea("Element Expressions");
		expressionsArea.setEditable(false);
		// elementExpressionPanel.add(expressionsArea);

		elementSimPropPanel = new JPanel();
		elementSimPropPanel.setLayout(new SpringLayout());

		configurator = new ConfiguratorPanel();
		
		configuratorProperties = new ConfigurationPropertiesTab();

		expressions = new RefasExpressionPanel(this, elm);

		// if (getPerspective() == 2) {

		// }

		// Bottom panel : Properties, Messages and Configuration
		extensionTabs = new JTabbedPane(JTabbedPane.TOP,
				JTabbedPane.SCROLL_TAB_LAYOUT);		
		extensionTabs.addTab(mxResources.get("elementExpressionTab"),
				new JScrollPane(expressions));
		extensionTabs.addTab(mxResources.get("messagesTab"), new JScrollPane(
				messagesArea));
		extensionTabs.addTab(mxResources.get("modelConfPropTab"),
				configuratorProperties.getScrollPane());
		extensionTabs.addTab(mxResources.get("configurationTab"),
				new JScrollPane(configurator));
		extensionTabs.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				((MainFrame) getFrame()).waitingCursor(true);
				if (((JTabbedPane) e.getSource()).getTabCount() > 3
						&& ((JTabbedPane) e.getSource()).getSelectedIndex() >= 0) {
					tabIndex = ((JTabbedPane) e.getSource()).getSelectedIndex();
					lastTabIndex = tabIndex;
					Component c = ((JTabbedPane) e.getSource())
							.getComponent(tabIndex);
					if (c != null) {
						c.revalidate();
						c.repaint();
					}
					String name = ((JTabbedPane) e.getSource())
							.getTitleAt(tabIndex);
					if (name.equals("Edit Expressions")
							&& editableElementType != null && perspective == 2
							&& updateExpressions) {
						if (elm instanceof InstConcept) {
							editableElementType = "vertex";
						}
						if (elm instanceof InstPairwiseRelation) {
							editableElementType = "edge";
						}
						if (elm instanceof InstOverTwoRelation) {
							editableElementType = "groupdep";
						}
						expressions.configure(getEditedModel(), refas2hlcl
								.getElementConstraintGroup(
										lastEditableElement.getIdentifier(),
										editableElementType),
								(InstElement) lastEditableElement);
						updateExpressions = false;
					}
				}
				// System.out.println(tabIndex);
				((MainFrame) getFrame()).waitingCursor(false);
			}

		});
		return extensionTabs;
	}

	private void updateVisibleProperties(final EditableElement elm) {
		extensionTabs.removeAll();
		if (elm != null) {
			// extensionTabs.addTab(mxResources.get("elementDisPropTab"),
			// new JScrollPane(elementDesPropPanel));
			extensionTabs.addTab(mxResources.get("elementDisPropTab"),
					new JScrollPane(elementDesignPanel));
			if (perspective == 2) {
				extensionTabs.addTab(mxResources.get("elementConfPropTab"),
						new JScrollPane(elementConfigPropPanel));
				// extensionTabs.addTab(mxResources.get("elementExpressionTab"),
				// new JScrollPane(elementExpressionPanel));
				extensionTabs.addTab(mxResources.get("elementSimPropTab"),
						new JScrollPane(elementSimPropPanel));
				extensionTabs.addTab(mxResources.get("elementExpressionTab"),
						new JScrollPane(expressionsArea));
			}
		}
		extensionTabs.addTab(mxResources.get("messagesTab"), new JScrollPane(
				messagesArea));
		extensionTabs.addTab(mxResources.get("editExpressionsTab"),
				new JScrollPane(expressions));
		extensionTabs.addTab(mxResources.get("modelConfPropTab"),
				configuratorProperties.getScrollPane());
		extensionTabs.addTab(mxResources.get("configurationTab"),
				new JScrollPane(configurator));
		
	}

	public void bringUpExtension(String name) {
		for (int i = 0; i < extensionTabs.getTabCount(); i++) {
			if (extensionTabs.getTitleAt(i).equals(name)) {
				extensionTabs.setSelectedIndex(i);
				return;
			}
		}
	}

	public void bringUpTab(String name) {
		for (int i = 0; i < extensionTabs.getTabCount(); i++) {
			if (extensionTabs.getTitleAt(i).equals(name)) {
				extensionTabs.setSelectedIndex(i);
				Component c = extensionTabs.getComponent(i);
				if (c != null) {
					c.revalidate();
					c.repaint();
					return;
				}
			}
		}
	}

	public JTextArea getMessagesArea() {
		return messagesArea;
	}

	public ConfiguratorPanel getConfigurator() {
		return configurator;
	}

	public void editModelReset() {
		productLineIndex.reset();
		if (perspective == 0)
			editModel(new ProductLine());
		else {
			// TODO fix when the syntax o semantic model is loaded -> update
			// dependent models.
			Refas refas = new Refas(PerspectiveType.modeling,
					((Refas) getEditedModel()).getSyntaxRefas(),
					((Refas) getEditedModel()).getSemanticRefas());
			refas2hlcl = new Refas2Hlcl(refas);
			editModel(refas);
			configurator.setRefas2hlcl(refas2hlcl);
		}

	}

	public void populateIndex(ProductLine pl) {

		// productLineIndex.populate(pl);
		AbstractGraph plGraph = (AbstractGraph) getGraphComponent().getGraph();
		plGraph.buildFromProductLine2(pl, productLineIndex);
		// ((mxGraphModel) plGraph.getModel()).clear();
		// plGraph.setProductLine(pl);

	}

	public AbstractModel getEditedModel() {
		if (perspective == 0)
			return ((AbstractGraph) getGraphComponent().getGraph())
					.getProductLine();
		else
			return ((AbstractGraph) getGraphComponent().getGraph()).getRefas();

	}

	// jcmunoz: new method for REFAS

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void editPropertiesRefas(final EditableElement elm) {
		try {

			updateVisibleProperties(elm);
			if (recursiveCall)
				return;
			elementDesignPanel.editorProperties(this, elm);
			this.extensionTabs.repaint();
			// elementDesPropPanel.removeAll();
			elementConfigPropPanel.removeAll();
			elementExpressionPanel.removeAll();
			elementSimPropPanel.removeAll();

			if (elm == null) {
				if (lastTabIndex != 0)
					lastTabIndex = 0;
				else {
					tabIndex = 0;
					extensionTabs.setSelectedIndex(0);

				}
				return;
			} else {
				recursiveCall = true;
				((MainFrame) getFrame()).waitingCursor(true);
				if (lastEditableElement != elm) {
					lastEditableElement = elm;
					// TODO workaround to update after simul
					updateExpressions = true;
				}
				if (extensionTabs.getTabCount() > tabIndex && tabIndex >= 0) {
					extensionTabs.setSelectedIndex(tabIndex);
					extensionTabs.getSelectedComponent().repaint();
				}
				JPanel elementConfPropSubPanel = new JPanel(new SpringLayout());
				JPanel elementSimPropSubPanel = new JPanel(new SpringLayout());

				List<InstAttribute> editables = elm.getEditableVariables();

				List<InstAttribute> visible = elm.getVisibleVariables();

				RefasWidgetFactory factory = new RefasWidgetFactory(this);

				int configurationPanelElements = 0, simulationPanelElements = 1;

				if (elm instanceof InstConcept) {
					editableElementType = "vertex";
				}
				if (elm instanceof InstPairwiseRelation) {
					if (((InstPairwiseRelation) elm).getSourceRelations()
							.size() == 0) {
						((MainFrame) getFrame()).waitingCursor(false);
						// TODO workaround for non supported relations - delete
						// after fix
						return;
					}

					editableElementType = "edge";
				}
				if (elm instanceof InstOverTwoRelation) {
					editableElementType = "groupdep";
				}
				if (editableElementType != null && this.perspective == 2) {
					expressionsArea.setText(refas2hlcl
							.getElementTextConstraints(elm.getIdentifier(),
									editableElementType));
					// expressions.configure(
					// getEditedModel(),
					// refas2hlcl.getElementConstraintGroup(
					// elm.getIdentifier(), type), (InstElement) elm);
				}
				JButton test = new JButton("Execute Simulation");
				elementSimPropSubPanel.add(test);
				elementSimPropSubPanel.add(new JPanel());
				elementSimPropSubPanel.add(new JPanel());
				test.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						executeSimulation(true);
					}
				});
				// TODO split in two new classes, one for each panel
				for (InstAttribute v : visible) {
					Map<String, MetaElement> mapElements = null;
					if (elm instanceof InstPairwiseRelation) {
						InstPairwiseRelation instPairwise = (InstPairwiseRelation) elm;
						mapElements = ((Refas) getEditedModel())
								.getSyntaxRefas()
								.getValidPairwiseRelations(
										instPairwise.getSourceRelations()
												.get(0).getSupportMetaElement(),
										instPairwise.getTargetRelations()
												.get(0).getSupportMetaElement(),
										true);
					}
					v.updateValidationList((InstElement) elm, mapElements);

					final WidgetR w = factory.getWidgetFor(v);

					if (w == null) {
						recursiveCall = false;
						System.err.print("No Widget found for " + v);
						return;
					}
					// TODO: Add listeners to w.

					w.getEditor().addFocusListener(new FocusListener() {
						@Override
						public void focusLost(FocusEvent arg0) {
							// Makes it pull the values.
							EditableElementAttribute v = w.getInstAttribute();
							if (v.getAttributeType().equals("String"))
								v.setValue(AbstractElement.multiLine(
										v.toString(), 15));
							// Divide lines every 15 characters (aprox.)
							onVariableEdited(elm, v);
						}

						@Override
						public void focusGained(FocusEvent arg0) {
						}
					});

					w.getEditor().addPropertyChangeListener(
							new PropertyChangeListener() {

								@Override
								public void propertyChange(
										PropertyChangeEvent evt) {
									if (WidgetPL.PROPERTY_VALUE.equals(evt
											.getPropertyName())) {
										w.getInstAttribute();
										updateExpressions = true;
										onVariableEdited(elm,
												w.getInstAttribute());
									}
								}
							});
					if (w instanceof MClassWidget
							|| w instanceof MEnumerationWidget) {
						w.getEditor().setPreferredSize(new Dimension(200, 100));
					} else {
						w.getEditor().setPreferredSize(new Dimension(200, 20));
						w.getEditor().setMaximumSize(new Dimension(200, 20));
					}
					w.editVariable(v);
					if (!editables.contains(v))
						w.getEditor().setEnabled(false);
					// GARA
					// variablesPanel.add(new JLabel(v.getName() + ":: "));
					if (v.getAttribute() instanceof SimulationStateAttribute) {
						elementSimPropSubPanel.add(new JLabel(v
								.getDisplayName() + ": "));
						elementSimPropSubPanel.add(w);

						if (v.isAffectProperties()) {
							JComponent wc = w.getEditor();
							if (wc instanceof ItemSelectable)
								((ItemSelectable) wc)
										.addItemListener(new ItemListener() {
											@Override
											public void itemStateChanged(
													ItemEvent e) {
												editPropertiesRefas(elm);
												updateExpressions = true;
											}
										});
							JButton button = new JButton("Validate");
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									if (!recursiveCall) {
										editPropertiesRefas(elm);
									}
								}
							});
							elementSimPropSubPanel.add(button);
						} else
							elementSimPropSubPanel.add(new JPanel());

						simulationPanelElements++;
					} else if (v.getAttribute() instanceof SimulationConfigAttribute) {
						elementConfPropSubPanel.add(new JLabel(v
								.getDisplayName() + ": "));
						elementConfPropSubPanel.add(w);

						if (v.isAffectProperties()) {
							JComponent wc = w.getEditor();
							if (wc instanceof ItemSelectable)
								((ItemSelectable) wc)
										.addItemListener(new ItemListener() {
											@Override
											public void itemStateChanged(
													ItemEvent e) {
												editPropertiesRefas(elm);
												updateExpressions = true;
											}
										});
							JButton button = new JButton("Validate");
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									editPropertiesRefas(elm);
								}
							});
							elementConfPropSubPanel.add(button);
						} else
							elementConfPropSubPanel.add(new JPanel());

						configurationPanelElements++;
					}

				}

				SpringUtilities.makeCompactGrid(elementSimPropSubPanel,
						simulationPanelElements / 2, 6, 4, 4, 4, 4);

				SpringUtilities.makeCompactGrid(elementConfPropSubPanel,
						configurationPanelElements, 3, 4, 4, 4, 4);

				elementConfigPropPanel.add(elementConfPropSubPanel);
				elementSimPropPanel.add(elementSimPropSubPanel);
				extensionTabs.getSelectedComponent().repaint();
				elementDesignPanel.revalidate();
				elementConfigPropPanel.revalidate();
				elementExpressionPanel.revalidate();
				elementSimPropPanel.revalidate();
			}
			((RefasGraph) getGraphComponent().getGraph()).refreshVariable(elm);
			((MainFrame) getFrame()).waitingCursor(false);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			recursiveCall = false;
		}
	}

	public void refreshElement(EditableElement elm) {
		List<InstAttribute> visible = elm.getVisibleVariables();
		RefasWidgetFactory factory = new RefasWidgetFactory(this);
		for (InstAttribute v : visible) {
			Map<String, MetaElement> mapElements = null;
			if (elm instanceof InstPairwiseRelation) {
				InstPairwiseRelation instPairwise = (InstPairwiseRelation) elm;
				mapElements = ((Refas) getEditedModel()).getSyntaxRefas()
						.getValidPairwiseRelations(
								instPairwise.getSourceRelations().get(0)
										.getSupportMetaElement(),
								instPairwise.getTargetRelations().get(0)
										.getSupportMetaElement(), true);
			}
			v.updateValidationList((InstElement) elm, mapElements);
			final WidgetR w = factory.getWidgetFor(v);
			if (w == null) {
				return;
			}
			w.editVariable(v);
		}
	}

	protected void onVariableEdited(Editable e) {
		((AbstractGraph) getGraphComponent().getGraph()).refreshVariable(e);
	}

	protected void onVariableEdited(EditableElement editableElement,
			EditableElementAttribute instAttribute) {
		if (editableElement instanceof InstConcept) {
			MetaElement editableMetaElement = ((InstConcept) editableElement)
					.getEditableMetaElement();
			if (editableMetaElement != null) {
				if (instAttribute.getIdentifier().equals("Identifier"))
					editableMetaElement.setIdentifier((String) instAttribute
							.getValue());
				if (instAttribute.getIdentifier().equals("Visible"))
					editableMetaElement.setVisible((boolean) instAttribute
							.getValue());
				if (instAttribute.getIdentifier().equals("Name"))
					editableMetaElement.setName((String) instAttribute
							.getValue());
				if (instAttribute.getIdentifier().equals("Style"))
					editableMetaElement.setStyle((String) instAttribute
							.getValue());
				if (instAttribute.getIdentifier().equals("Description"))
					editableMetaElement.setDescription((String) instAttribute
							.getValue());
				if (instAttribute.getIdentifier().equals("Width"))
					editableMetaElement
							.setWidth((int) instAttribute.getValue());
				if (instAttribute.getIdentifier().equals("Height"))
					editableMetaElement.setHeight((int) instAttribute
							.getValue());
				if (instAttribute.getIdentifier().equals("Image"))
					editableMetaElement.setImage((String) instAttribute
							.getValue());
				if (instAttribute.getIdentifier().equals("TopConcept"))
					((MetaConcept) editableMetaElement)
							.setTopConcept((boolean) instAttribute.getValue());
				if (instAttribute.getIdentifier().equals("BackgroundColor"))
					((MetaConcept) editableMetaElement)
							.setBackgroundColor((String) instAttribute
									.getValue());
				if (instAttribute.getIdentifier().equals("BorderStroke"))
					editableMetaElement.setBorderStroke((int) instAttribute
							.getValue());
				if (instAttribute.getIdentifier().equals("Resizable"))
					((MetaConcept) editableMetaElement)
							.setResizable((boolean) instAttribute.getValue());
				if (instAttribute.getIdentifier().equals("value"))
					editableMetaElement
							.setModelingAttributes((Map<String, AbstractAttribute>) instAttribute
									.getValue());
			}
			IntSemanticElement editableSemanticElement = ((InstConcept) editableElement)
					.getEditableSemanticElement();
			if (editableSemanticElement != null) {
				if (instAttribute.getIdentifier().equals("Identifier"))
					editableSemanticElement
							.setIdentifier((String) instAttribute.getValue());
			}
		}
		((RefasGraph) getGraphComponent().getGraph())
				.refreshVariable(editableElement);
	}

	protected void installToolBar(MainFrame mainFrame, int perspective) {

		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		if (perspective == 3)
			jp.add(new RefasEditorToolBar(this, JToolBar.HORIZONTAL),
					BorderLayout.WEST);
		else
			jp.add(new PLEditorToolBar(this, JToolBar.HORIZONTAL),
					BorderLayout.WEST);
		jp.add(new JLabel(), BorderLayout.CENTER);
		if (mainFrame != null)
			perspectiveToolBar = new PerspectiveToolBar(mainFrame,
					JToolBar.HORIZONTAL, perspective);
		else
			perspectiveToolBar = new PerspectiveToolBar(this,
					JToolBar.HORIZONTAL, perspective);
		jp.add(perspectiveToolBar, BorderLayout.EAST);
		add(jp, BorderLayout.NORTH);
	}

	public final MainFrame getMainFrame() {
		Container contairner1 = this.getParent();

		Container contairner2 = contairner1.getParent();

		Container contairner3 = contairner2.getParent();

		Container contairner4 = contairner3.getParent();

		return (MainFrame) contairner4;

	}

	public void refreshPalette() {
		int i = graphAndRight.getDividerLocation();
		graphAndRight.setDividerLocation(i + 1);
	}

	public void setModelEditor(VariamosGraphEditor modelEditor) {
		this.modelEditor = modelEditor;
	}

	public void updateObjects() {
		if (perspective == 4) {
			this.graphComponent.setGraph(modelEditor.getGraphComponent()
					.getGraph());

			// mxGraphModel.prototype.cloneCells
			/*
			 * Object parent =
			 * modelEditor.getGraphComponent().getGraph().getDefaultParent();
			 * modelEditor.getGraphComponent().getGraph().selectAll(); Object[]
			 * all =
			 * modelEditor.getGraphComponent().getGraph().getSelectionCells();
			 * modelEditor.getGraphComponent().getGraph().addCell(all);
			 * this.graphComponent.getGraph().selectAll();
			 * this.graphComponent.getGraph().addCells(all);
			 */}

	}

	public void executeSimulation(boolean first) {
		((MainFrame) getFrame()).waitingCursor(true);
		boolean result = false;
		if (first)
			result = refas2hlcl.execute(Refas2Hlcl.ONE_SOLUTION);
		else
			result = refas2hlcl.execute(Refas2Hlcl.NEXT_SOLUTION);
		if (result) {
			refas2hlcl.updateGUIElements();
			messagesArea.setText(refas2hlcl.getText());
			bringUpTab(mxResources.get("elementSimPropTab"));
			editPropertiesRefas(lastEditableElement);
		} else {
			if (first) {
				JOptionPane
						.showMessageDialog(
								frame,
								"No solution found for this model configuration."
										+ " \n Please review the restrictions defined and "
										+ "try again. \nAttributes values were not updated.",
								"Simulation Execution Error",
								JOptionPane.INFORMATION_MESSAGE, null);
			} else
				JOptionPane.showMessageDialog(frame, "No more solutions found",
						"Simulation Message", JOptionPane.INFORMATION_MESSAGE,
						null);

		}
		if (lastEditableElement == null)
			JOptionPane
					.showMessageDialog(
							frame,
							"Select any element and after execute the simulation.",
							"Simulation Message",
							JOptionPane.INFORMATION_MESSAGE, null);
		else
			((RefasGraph) getGraphComponent().getGraph())
					.refreshVariable(lastEditableElement);
		updateObjects();
		((MainFrame) getFrame()).waitingCursor(false);
	}
	// public void editProperties(final Editable elm) {
	//
	// // elementDesPropPanel.removeAll();
	// elementConfigPropPanel.removeAll();
	// elementExpressionPanel.removeAll();
	// elementSimPropPanel.removeAll();
	//
	// if (elm == null) {
	// bringUpTab("Properties");
	//
	// // elementDesPropPanel.repaint();
	// elementConfigPropPanel.repaint();
	// elementExpressionPanel.repaint();
	// elementSimPropPanel.repaint();
	// return;
	// }
	//
	// JPanel variablesPanel = new JPanel(new SpringLayout());
	//
	// Variable[] editables = elm.getEditableVariables();
	//
	// WidgetFactory factory = new WidgetFactory(this);
	// for (Variable v : editables) {
	// final WidgetPL w = factory.getWidgetFor(v);
	// if (w == null)
	// // Check the problem and/or raise an exception
	// return;
	//
	// // TODO: Add listeners to w.
	// w.getEditor().addFocusListener(new FocusListener() {
	// @Override
	// public void focusLost(FocusEvent arg0) {
	// // Makes it pull the values.
	// Variable v = w.getVariable();
	// if (v.getType().equals("String"))
	// v.setValue(AbstractElement.multiLine(v.toString(), 15));
	// System.out.println("Focus Lost: " + v.hashCode() + " val: "
	// + v.getValue());
	// onVariableEdited(elm);
	// }
	//
	// @Override
	// public void focusGained(FocusEvent arg0) {
	//
	// }
	// });
	//
	// w.getEditor().addPropertyChangeListener(
	// new PropertyChangeListener() {
	//
	// @Override
	// public void propertyChange(PropertyChangeEvent evt) {
	// if (WidgetPL.PROPERTY_VALUE.equals(evt
	// .getPropertyName())) {
	// w.getVariable();
	// onVariableEdited(elm);
	// }
	// }
	// });
	// w.getEditor().setMinimumSize(new Dimension(50, 30));
	// w.getEditor().setMaximumSize(new Dimension(200, 30));
	// w.getEditor().setPreferredSize(new Dimension(200, 30));
	// w.editVariable(v);
	//
	// // GARA
	// // variablesPanel.add(new JLabel(v.getName() + ":: "));
	// variablesPanel.add(new JLabel(v.getName() + ": "));
	// variablesPanel.add(w);
	// }
	// // variablesPanel.setPreferredSize(new Dimension(250, 25 *
	// // editables.length));
	// SpringUtilities.makeCompactGrid(variablesPanel, editables.length, 2, 4,
	// 4, 4, 4);
	//
	// // elementDesPropPanel.add(variablesPanel);
	//
	// // JPanel attPanel = new JPanel(new SpringLayout());
	// // // Fill Attributes Panel (Only for VariabilityElements ) in Properties
	// // // Panel
	// // if (elm instanceof VariabilityElement) {
	// // attPanel.setPreferredSize(new Dimension(150, 150));
	// // attPanel.add(new JLabel(mxResources.get("attributesPanel")));
	// //
	// // VariabilityAttributeList attList = new VariabilityAttributeList(
	// // this, (VariabilityElement) elm);
	// // attPanel.add(new JScrollPane(attList));
	// //
	// // SpringUtilities.makeCompactGrid(attPanel, 2, 1, 4, 4, 4, 4);
	//
	// // elementDesPropPanel.add(attPanel);
	//
	// // SpringUtilities.makeCompactGrid(elementDesPropPanel, 1, 2, 4, 4, 4,
	// 4);
	// }
	//
	// elementDesPropPanel.revalidate();
	// }

}
