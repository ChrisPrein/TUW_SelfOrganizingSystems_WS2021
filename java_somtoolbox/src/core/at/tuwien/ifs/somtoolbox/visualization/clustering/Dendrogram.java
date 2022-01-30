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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import at.tuwien.ifs.somtoolbox.apps.viewer.controls.ClusteringControl;

/**
 * @author Taylor Peer
 * @author Michael Wagner
 * @version $Id: Dendrogram.java 4339 2015-03-06 15:17:08Z mayer $
 */
public class Dendrogram {

    public static Map<Integer, Integer> levelXThresholds = new TreeMap<Integer, Integer>();

    public static Map<Integer, Integer> levelClusterCounts = new TreeMap<Integer, Integer>();

    public static int getClustersAtClickedLevel(int x) {
        for (Map.Entry<Integer, Integer> entry : levelXThresholds.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            if (x > value) {
                return getClusterCountAboveLevel(key);
            }
        }
        return 0;
    }

    private static int getClusterCountAboveLevel(int level) {
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : levelClusterCounts.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            if (key >= level) {
                count = count + value;
            }
        }
        return count;
    }

    public static void createAndShowGUI(ClusterNode topNode) {

        // Reset level thresholds
        levelXThresholds = new TreeMap<Integer, Integer>();

        JFrame f = new JFrame();

        DendrogramPaintPanel panel = new DendrogramPaintPanel(topNode);

        JScrollPane scrollPane = new JScrollPane(panel);

        scrollPane.setPreferredSize(new Dimension(panel.getWidth(), 800));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int level = getClustersAtClickedLevel(e.getX());
                ClusteringControl.getInstance().getSpinnerNoCluster().setValue(level);
            }
        });

        f.getContentPane().add(scrollPane);

        f.setSize(1000, 800);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}

class DendrogramPaintPanel extends JPanel {

    private static final long serialVersionUID = 1142616885007793370L;

    private ClusterNode root;

    private int leaves;

    private int levels;

    private int heightPerLeaf;

    private int widthPerLevel;

    private int currentY;

    private static final int MIN_HEIGHT_PER_LEAF = 4;

    private int margin = 25;

    DendrogramPaintPanel(ClusterNode topNode) {
        this.root = topNode;
    }

    private static int getNumChildren(ClusterNode node) {

        int count = 0;
        if (node == null) {
            return count;
        }

        ClusterNode child1 = node.getChild1();
        ClusterNode child2 = node.getChild2();
        if (child1 != null) {
            count++;
        }
        if (child2 != null) {
            count++;
        }
        return count + getNumChildren(child1) + getNumChildren(child1);
    }

    private static int countLeaves(ClusterNode node) {
        if (getNumChildren(node) == 0) {
            return 1;
        }
        ClusterNode child1 = node.getChild1();
        ClusterNode child2 = node.getChild2();
        return countLeaves(child1) + countLeaves(child2);
    }

    private static int countLevels(ClusterNode node) {
        if (getNumChildren(node) == 0) {
            return 1;
        }
        ClusterNode child1 = node.getChild1();
        ClusterNode child2 = node.getChild2();
        return 1 + Math.max(countLevels(child1), countLevels(child2));
    }

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);

        Dendrogram.levelClusterCounts = new TreeMap<Integer, Integer>();

        Graphics2D g = (Graphics2D) gr;
        int margin = 5;
        leaves = countLeaves(root);
        levels = countLevels(root);
        heightPerLeaf = (int) Math.round(((double) getHeight() - margin - margin) / leaves);

        if (heightPerLeaf < MIN_HEIGHT_PER_LEAF) {
            heightPerLeaf = MIN_HEIGHT_PER_LEAF;
        }

        setPreferredSize(new Dimension(getWidth(), calculateHeight()));

        widthPerLevel = (int) Math.round(((double) getWidth() - margin - margin) / levels);
        currentY = 0;

        g.translate(margin, margin);
        draw(g, root, 0);
    }

    private int calculateHeight() {
        return heightPerLeaf * leaves + 2 * margin;
    }

    private <T> Point draw(Graphics g, ClusterNode node, int y) {
        if (getNumChildren(node) == 0) {
            int x = getWidth() - widthPerLevel;
            int resultX = x;
            int resultY = currentY;
            currentY += heightPerLeaf;
            return new Point(resultX, resultY);
        } else if (getNumChildren(node) >= 2) {
            ClusterNode child1 = node.getChild1();
            ClusterNode child2 = node.getChild2();
            Point p0 = draw(g, child1, y);
            Point p1 = draw(g, child2, y + heightPerLeaf);

            // Update X-thresholds of diagram levels
            Dendrogram.levelXThresholds.put(countLevels(child1), p0.x);
            Dendrogram.levelXThresholds.put(countLevels(child2), p1.x);

            // Update number of clusters in each level
            int child1Level = countLevels(child1);
            if (Dendrogram.levelClusterCounts.containsKey(child1Level)) {
                Integer countAtLevel = Dendrogram.levelClusterCounts.get(child1Level);
                countAtLevel++;
                Dendrogram.levelClusterCounts.put(child1Level, countAtLevel);
            } else {
                Dendrogram.levelClusterCounts.put(child1Level, 1);
            }
            int child2Level = countLevels(child2);
            if (Dendrogram.levelClusterCounts.containsKey(child2Level)) {
                Integer countAtLevel = Dendrogram.levelClusterCounts.get(child2Level);
                countAtLevel++;
                Dendrogram.levelClusterCounts.put(child2Level, countAtLevel);
            } else {
                Dendrogram.levelClusterCounts.put(child2Level, 1);
            }

            g.fillRect(p0.x - 2, p0.y - 2, 4, 4);
            g.fillRect(p1.x - 2, p1.y - 2, 4, 4);
            int dx = widthPerLevel;
            int vx = Math.min(p0.x - dx, p1.x - dx);
            g.drawLine(vx, p0.y, p0.x, p0.y);
            g.drawLine(vx, p1.y, p1.x, p1.y);
            g.drawLine(vx, p0.y, vx, p1.y);
            Point p = new Point(vx, p0.y + (p1.y - p0.y) / 2);
            return p;
        }
        return new Point();
    }
}