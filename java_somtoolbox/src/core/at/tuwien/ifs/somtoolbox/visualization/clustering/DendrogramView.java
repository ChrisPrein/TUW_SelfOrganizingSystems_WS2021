/*
 * Copyright 2004-2010 Institute of Software Technology and Interactive Systems, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.tuwien.ifs.somtoolbox.visualization.clustering;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

import at.tuwien.ifs.somtoolbox.util.UiUtils;
import at.tuwien.ifs.somtoolbox.visualization.Palette;
import at.tuwien.ifs.somtoolbox.visualization.Palettes;

/**
 * @author Tatiana Rybnikova
 * @author Tomas Sedivy
 * @version $Id: DendrogramView.java 4351 2015-03-10 16:14:44Z mayer $
 */
public class DendrogramView {

    // private static final Logger log = Logger.getLogger(DendrogramView.class.getName());

    protected Palette palette = Palettes.getPaletteByName(getPreferredPaletteName());

    private static final float MAX_STROKE_WIDTH = 1;

    private static final float DEFAULT_STROKE_WIDTH = 0.1f;

    private JFrame frame;

    // private JLabel lblNewLabel;

    private int nodeId = 0;

    private Double mMergeCostMax = null;

    private Double mMergeCostMin = null;

    private Double mMergeCostSumMax = null;

    private Double mMergeCostSumMin = null;

    private JSpinner mMainSpinnerCluster = null;

    private JSpinner spinnerCluster;

    private JPanel clusterPanel;

    private int maxCluster = 0;

    /**
     * Create the application.
     */
    public DendrogramView() {
        initialize();
    }

    /**
     * Create the application.
     */
    public DendrogramView(int maxCluster, JSpinner _cs) {
        initialize();
        this.maxCluster = maxCluster;
        this.mMainSpinnerCluster = _cs;
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 578, 414);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public void update(ClusteringTree t, int numClusters) {

        frame.setVisible(true);

        frame.getContentPane().removeAll();
        try {
            // create spinner-panel
            clusterPanel = UiUtils.makeBorderedPanel(new FlowLayout(FlowLayout.LEFT, 10, 0), "Clusters");
            spinnerCluster = new JSpinner(new SpinnerNumberModel(1, 1, maxCluster, 1));
            spinnerCluster.setValue(numClusters);
            spinnerCluster.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mMainSpinnerCluster != null) {
                        mMainSpinnerCluster.setValue(((JSpinner) e.getSource()).getValue());
                    }
                }
            });
            UiUtils.fillPanel(clusterPanel, new JLabel("#"), spinnerCluster);
            frame.getContentPane().add(clusterPanel, BorderLayout.NORTH);
            // create graph
            VisualizationViewer<DendrogramChild, DendrogramEdge> g = generateGraph(t, numClusters);
            frame.getContentPane().add(g, BorderLayout.CENTER);
            frame.setSize(frame.getWidth() + 1, frame.getHeight() + 1);
            frame.setSize(frame.getWidth() - 1, frame.getHeight() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private VisualizationViewer<DendrogramChild, DendrogramEdge> generateGraph(ClusteringTree t, final int numClusters) {

        DelegateTree<DendrogramChild, DendrogramEdge> g = new DelegateTree<DendrogramChild, DendrogramEdge>();

        ClusterNode root = t.findNode(1);

        mMergeCostMax = null;
        mMergeCostMin = null;
        mMergeCostSumMax = null;
        mMergeCostSumMin = null;

        calcMergeCostSum(root, root.getMergeCost(), numClusters);

        findMinMaxMergeCost(root, true, numClusters);

        nodeId = 0;
        DendrogramChild rc = new DendrogramChild(root);

        addChildren(root, g, rc, numClusters);
        Layout<DendrogramChild, DendrogramEdge> layout = new TreeLayout<DendrogramChild, DendrogramEdge>(g);

        VisualizationViewer<DendrogramChild, DendrogramEdge> graphComponent = new VisualizationViewer<DendrogramChild, DendrogramEdge>(
                layout);
        DefaultModalGraphMouse<DendrogramChild, DendrogramEdge> gm = new DefaultModalGraphMouse<DendrogramChild, DendrogramEdge>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        graphComponent.setGraphMouse(gm);

        graphComponent.enableInputMethods(true);

        RenderContext<DendrogramChild, DendrogramEdge> c = graphComponent.getRenderContext();
        graphComponent.setVertexToolTipTransformer(new Transformer<DendrogramChild, String>() {

            @Override
            public String transform(DendrogramChild c) {
                if (c.getNode().getMergeCost() == null) {
                    return "count:" + c.getNode().getNodes().length + " " + c.getTooltip();
                } else {
                    return "merge cotst:" + c.getNode().getMergeCost() + " count:" + c.getNode().getNodes().length
                            + " " + c.getTooltip();
                }
            }
        });

        graphComponent.setSize(frame.getContentPane().getWidth(),
                frame.getContentPane().getHeight() - clusterPanel.getHeight());

        // change edge-stroke-thickness
        c.setEdgeStrokeTransformer(new Transformer<DendrogramEdge, Stroke>() {

            @Override
            public Stroke transform(DendrogramEdge _arg0) {
                return new BasicStroke(_arg0.getMergeCost().floatValue() * MAX_STROKE_WIDTH);
            }
        });
        final Rectangle fake = new Rectangle(1, 1);
        c.setEdgeArrowTransformer(new Transformer<Context<Graph<DendrogramChild, DendrogramEdge>, DendrogramEdge>, Shape>() {

            @Override
            public Shape transform(Context<Graph<DendrogramChild, DendrogramEdge>, DendrogramEdge> _arg0) {
                return fake;
            }
        });
        // change vertex-fill-color
        c.setVertexFillPaintTransformer(new Transformer<DendrogramChild, Paint>() {

            @Override
            public Paint transform(DendrogramChild _arg0) {
                if (_arg0.getPaint() == null) {
                    if (_arg0.getMergeCostSum() != null) {
                        double normalized = getNormalizedValue(_arg0.getMergeCostSum(), mMergeCostSumMax,
                                mMergeCostSumMin, _arg0.getMergeCostSum());
                        int cc = (int) Math.round(normalized * (palette.getColors().length - 1));
                        return palette.getColor(cc);
                    } else {
                        return palette.getColor(palette.getColors().length - 1);
                    }
                } else {
                    return _arg0.getPaint();
                }
            }
        });

        return graphComponent;
    }

    private void addChildren(ClusterNode n, DelegateTree<DendrogramChild, DendrogramEdge> g, DendrogramChild parent,
            int numClusters) {
        nodeId++;

        if (g.getVertexCount() == 0) {
            g.setRoot(parent);
            if (n.getLevel() < numClusters) {

                if (n.getChild1() != null) {
                    addChildren(n.getChild1(), g, parent, numClusters);
                }
                if (n.getChild2() != null) {

                    addChildren(n.getChild2(), g, parent, numClusters);
                }

            }
            return;
        }
        DendrogramChild child = new DendrogramChild(n);
        double mergecost = n.getMergeCost() != null ? n.getMergeCost() : 1.0;

        g.addChild(
                new DendrogramEdge(nodeId, getNormalizedValue(mergecost, mMergeCostMax, mMergeCostMin,
                        DEFAULT_STROKE_WIDTH)), parent, child);
        if (n.getLevel() < numClusters) {

            if (n.getChild1() != null) {
                addChildren(n.getChild1(), g, child, numClusters);
            }
            if (n.getChild2() != null) {
                addChildren(n.getChild2(), g, child, numClusters);
            }
        }
    }

    private void findMinMaxMergeCost(ClusterNode root, boolean isRoot, int numClusters) {
        if (root != null) {
            Double m = root.getMergeCost();
            Double mSum = root.getMergeCostSum();
            if (m != null) {
                if (!isRoot) {
                    if (mSum != null) {
                        if (mMergeCostSumMin != null) {
                            mMergeCostSumMax = mMergeCostSumMax == null || mSum > mMergeCostSumMax ? mSum
                                    : mMergeCostSumMax;
                        }
                        mMergeCostSumMin = mMergeCostSumMin == null || mSum < mMergeCostSumMin ? mSum
                                : mMergeCostSumMin;
                    }
                    mMergeCostMax = mMergeCostMax == null || m > mMergeCostMax ? m : mMergeCostMax;
                    mMergeCostMin = mMergeCostMin == null || m < mMergeCostMin ? m : mMergeCostMin;
                }

                findMinMaxMergeCost(root.getChild1(), false, numClusters);
                findMinMaxMergeCost(root.getChild2(), false, numClusters);
            }
        }
    }

    private Double getNormalizedValue(double value, Double max, Double min, double default_val) {
        if (max != null) {
            return (value - min) / (max - min);
        }
        return default_val;
    }

    private void calcMergeCostSum(ClusterNode root, Double mergeCostParent, int numClusters) {
        if (root != null && mergeCostParent != null) {

            ClusterNode c1 = root.getChild1();

            if (c1 != null) {
                if (c1.getMergeCost() != null) {
                    c1.setMergeCostSum(mergeCostParent + c1.getMergeCost());
                } else {
                    c1.setMergeCostSum(mergeCostParent);
                }
                if (c1.getLevel() <= numClusters) {
                    calcMergeCostSum(c1, c1.getMergeCostSum(), numClusters);
                }
            }

            ClusterNode c2 = root.getChild2();

            if (c2 != null) {
                if (c2.getMergeCost() != null) {
                    c2.setMergeCostSum(mergeCostParent + c2.getMergeCost());
                } else {
                    c2.setMergeCostSum(mergeCostParent);
                }
                if (c2.getLevel() <= numClusters) {
                    calcMergeCostSum(c2, c2.getMergeCostSum(), numClusters);
                }
            }
        }
    }

    public String getPreferredPaletteName() {
        return Palettes.getDefaultPalette().getName();
    }
}
