package edu.jwetherell.algorithms.dataStructures.intervalTree;

import edu.jwetherell.algorithms.dataStructures.IntervalTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Nitin Verma
 * Date: 23/04/13
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntervalTreeTest {

    private static final Log LOGGER = LogFactory.getLog(IntervalTreeTest.class);

    private class Rectangle implements Comparable<Rectangle> {
        private final Line xLine, yLine;

        public Rectangle(final Line xLine, final Line yLine) {
            this.xLine = xLine;
            this.yLine = yLine;
        }

        public Line getXLine() {
            return xLine;
        }

        public Line getYLine() {
            return yLine;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "[" + xLine + "," + yLine + "]";
        }

        @Override
        public int compareTo(final Rectangle otherRectangle) {
            int comparison = yLine.compareTo(otherRectangle.yLine);
            if (comparison == 0) {
                comparison = xLine.compareTo(otherRectangle.xLine);
            }
            return comparison;
        }
    }

    private class Line implements Comparable<Line> {
        private final long start, end;

        public Line(final long point1, final long point2) {
            if (point1 < point2) {
                this.start = point1;
                this.end = point2;
            }
            else {
                this.start = point2;
                this.end = point1;
            }
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "[" + start + "," + end + "]";
        }

        @Override
        public int compareTo(final Line otherLine) {
            int comparison = compareLong(getStart(), otherLine.getStart());
            if (comparison == 0) {
                comparison = compareLong(getEnd(), otherLine.getEnd());
            }
            return comparison;
        }

        private int compareLong(final long l1, final long l2) {
            int comparison = 0;
            if(l1 > l2) {
                comparison = 1;
            }
            else if (l1 < l2){
                comparison = -1;
            }
            return comparison;
        }
    }

    private class ListComparator<T extends Comparable> implements Comparator<List<T>> {

        @Override
        public int compare(final List<T> ts1, final List<T> ts2) {

            if (ts1 == null || ts2 == null ) {
                throw new NullPointerException("Non of the compared lists can be null");
            }

            int comparison = compareIntegers(ts1.size(), ts2.size());

            if (comparison == 0) {
                for (int i = 0; i < ts1.size(); i++) {
                    final T value1 = ts1.get(i);
                    final T value2 = ts2.get(i);

                    if (value1 == null || value2 == null) {
                        throw new NullPointerException("Non of the compared elements can be null");
                    }
                    comparison = value1.compareTo(value2);
                    if (comparison != 0) {
                        break;
                    }
                }
            }

            LOGGER.debug("Comparison: " + comparison);

            return comparison;
        }

        public int compareIntegers(final Integer i1, final Integer i2) {
            return i1.compareTo(i2);
        }
    }

    private class RectangularIntervalTree {

        private final IntervalTree<List<Rectangle>> xIntervalTree, yIntervalTree;

        public RectangularIntervalTree(final Set<Rectangle> rectangularInterval) {
            xIntervalTree = createXIntervalTree(rectangularInterval);
            yIntervalTree = createYIntervalTree(rectangularInterval);
        }

        private IntervalTree<List<Rectangle>> createXIntervalTree(
                final Set<Rectangle> rectangularInterval) {

            return createIntervalTree(rectangularInterval, true);
        }

        private IntervalTree<List<Rectangle>> createYIntervalTree(
                final Set<Rectangle> rectangularInterval) {

            return createIntervalTree(rectangularInterval, false);
        }

        private IntervalTree<List<Rectangle>> createIntervalTree(final Set<Rectangle> rectangularInterval,
                                                                 final boolean processX) {

            final java.util.List<IntervalTree.IntervalData<List<Rectangle>>>
                    intervals = new ArrayList<IntervalTree.IntervalData<List<Rectangle>>>();

            final Map<Line, List<Rectangle>> tmpLineMap = new java.util.TreeMap<Line, List<Rectangle>>();

            for (final Rectangle rectangle : rectangularInterval) {
                final Line key = processX ? rectangle.getXLine() : rectangle.getXLine();
                put(tmpLineMap, rectangle, key);
            }

            for ( final Line line : tmpLineMap.keySet() ) {
                intervals.add(
                        new IntervalTree.IntervalData<List<Rectangle>>(
                                line.getStart(), line.getEnd(), tmpLineMap.get(line)
                        )
                );
            }

            return new IntervalTree<List<Rectangle>>(intervals);
        }

        private void put(final Map<Line, List<Rectangle>> tmpLineMap,
                         final Rectangle rectangle,
                         final Line key) {

            if ( tmpLineMap.containsKey(key) ) {
                tmpLineMap.get(key).add(rectangle);
            }
            else {
                final List<Rectangle> rectangleList = new ArrayList<Rectangle>();
                rectangleList.add(rectangle);
                tmpLineMap.put(key, rectangleList);
            }

        }

        private Set<Rectangle> query(final Rectangle queryRectangle,
                                     final boolean considerOverlap) {

            Set<Rectangle> result = new TreeSet<Rectangle>();
            final Set<Rectangle> xResult = query(queryRectangle, considerOverlap, true);
            final Set<Rectangle> yResult = query(queryRectangle, considerOverlap, false);
            LOGGER.info("xResult rectangles: " + xResult);
            LOGGER.info("yResult rectangles: " + yResult);
            // find intersection of xResult and yResult'
            result = Sets.intersection(xResult, yResult);
            return result;

        }

        private Set<Rectangle> query(final Rectangle queryRectangle,
                                     final boolean considerOverlap,
                                     final boolean processX) {

            Set<Rectangle> result = new TreeSet<Rectangle>();

            long start = queryRectangle.getYLine().getStart();
            long end = queryRectangle.getYLine().getEnd();
            IntervalTree intervalTree = yIntervalTree;

            if (processX) {
                start = queryRectangle.getXLine().getStart();
                end = queryRectangle.getXLine().getEnd();
                intervalTree = xIntervalTree;
            }
            final ListComparator<Rectangle> rectangleListComparator = new ListComparator<Rectangle>();

            final IntervalTree.IntervalData<List<Rectangle>> intervalData =
                    intervalTree.query(start, end, rectangleListComparator);

            LOGGER.debug( (processX ? "X" : "Y") + " Interval Data : " + intervalData);

            final Set<List<Rectangle>> matches = intervalData.matches();

            // flatten
            for(final List<Rectangle> rectangleList: matches) {
                result.addAll(rectangleList);
            }

            final Set<Rectangle> resultCopy = new TreeSet<Rectangle>(result);
            // trim overlaps
            if (!considerOverlap) {
                for(final Rectangle rectangle : resultCopy) {
                    boolean drop = processX
                            ? rectangle.getXLine().getStart() < start
                            : rectangle.getYLine().getStart() < start;
                    if (!drop) {
                        drop = processX
                                ? rectangle.getXLine().getEnd() > end
                                : rectangle.getYLine().getEnd() > end;

                    }
                    if (drop) {
                        result.remove(rectangle);
                        LOGGER.info( (processX ? "X" : "Y") + " : Removing rectangle: " + rectangle);
                    }
                }
            }

            return result;
        }
    }

    @Test
    public void rectangularInterval() {
        final Set<Rectangle> rectangularInterval = new TreeSet<Rectangle>();
        rectangularInterval.add(new Rectangle(new Line(1,3), new Line(1,3)));
        rectangularInterval.add(new Rectangle(new Line(4,5), new Line(3,4)));
        rectangularInterval.add(new Rectangle(new Line(6,7), new Line(3,4)));
        rectangularInterval.add(new Rectangle(new Line(2,3), new Line(5,6)));
        rectangularInterval.add(new Rectangle(new Line(4,5), new Line(5,6)));
        rectangularInterval.add(new Rectangle(new Line(6,7), new Line(5,6)));
        rectangularInterval.add(new Rectangle(new Line(5,7), new Line(7,9)));
        rectangularInterval.add(new Rectangle(new Line(7,8), new Line(8,9)));

        final RectangularIntervalTree RectangularIntervalTree =
                new RectangularIntervalTree(rectangularInterval);

        final Set<Rectangle> result = RectangularIntervalTree.query(
                new Rectangle(new Line(2,8), new Line(2,8)),false);
        LOGGER.info("Result: " + result);
    }


    @Test
    public void oneDInterval() {
        java.util.List<IntervalTree.IntervalData<String>> intervals = new ArrayList<IntervalTree.IntervalData<String>>();
        intervals.add((new IntervalTree.IntervalData<String>(0, 1, "0-1")));
        intervals.add((new IntervalTree.IntervalData<String>(2, 3, "2-3")));
        intervals.add((new IntervalTree.IntervalData<String>(3, 4, "3-4(1)")));
        intervals.add((new IntervalTree.IntervalData<String>(3, 4, "3-4(2)")));
        intervals.add((new IntervalTree.IntervalData<String>(3, 15, "3-15")));
        intervals.add((new IntervalTree.IntervalData<String>(5, 6, "5-6")));
        intervals.add((new IntervalTree.IntervalData<String>(4, 99, "4-99")));

        IntervalTree<String> tree = new IntervalTree<String>(intervals);
        LOGGER.info( tree.query(2,4) );
    }
}
