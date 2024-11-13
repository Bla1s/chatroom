package editorkit;

import javax.swing.*;
import javax.swing.text.*;

public class WrapEditorKit extends StyledEditorKit {
    private ViewFactory defaultFactory = new WrapColumnFactory();

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    private static class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new WrapLabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new CustomParagraphView(elem);
                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    private static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }

        @Override
        public int getBreakWeight(int axis, float pos, float len) {
            if (axis == View.X_AXIS) {
                int p0 = getStartOffset();
                int p1 = getEndOffset();
                String text = "";
                try {
                    text = getDocument().getText(p0, p1 - p0);
                } catch (BadLocationException e) {
                    return BadBreakWeight;
                }

                // Allow breaking at word boundaries
                char ch = text.charAt(Math.min(text.length() - 1, (int)pos));
                if (Character.isWhitespace(ch)) {
                    return GoodBreakWeight;
                }
                return super.getBreakWeight(axis, pos, len);
            }
            return super.getBreakWeight(axis, pos, len);
        }

        @Override
        public View breakView(int axis, int p0, float pos, float len) {
            if (axis == View.X_AXIS) {
                checkPainter();
                int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                if (p1 == getStartOffset()) {
                    return this;
                }
                return createFragment(p0, p1);
            }
            return this;
        }
    }

    private static class CustomParagraphView extends ParagraphView {
        public CustomParagraphView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }

        @Override
        protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
            if (r == null) {
                r = new SizeRequirements();
            }
            float pref = layoutPool.getPreferredSpan(axis);
            float min = layoutPool.getMinimumSpan(axis);
            r.minimum = (int) min;
            r.preferred = Math.max(r.minimum, (int) pref);
            r.maximum = Integer.MAX_VALUE;
            r.alignment = 0.5f;
            return r;
        }

        @Override
        public float nextTabStop(float x, int tabOffset) {
            return x;
        }
    }
}