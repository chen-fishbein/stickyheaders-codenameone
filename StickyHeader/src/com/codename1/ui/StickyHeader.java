/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.ui;

import com.codename1.ui.Container;
import com.codename1.ui.Graphics;
import com.codename1.ui.Painter;
import com.codename1.ui.events.ScrollListener;
import com.codename1.ui.geom.Rectangle;
import java.util.Stack;

/**
 *
 * @author Chen
 */
public class StickyHeader extends Container implements ScrollListener {

    private int previousPosition;
    
    private boolean needToCheck = false;

    public StickyHeader() {
    }

    @Override
    protected void initComponent() {
        super.initComponent();
        Container p = getParent();
        p.addScrollListener(this);
        previousPosition = getParent().getAbsoluteY() - getAbsoluteY();
    }

    @Override
    protected void laidOut() {
        if(getY() == 0){
            needToCheck = true;
        }
    }
    
    

    @Override
    public void scrollChanged(int scrollX, int scrollY, int oldscrollX, int oldscrollY) {
        int position = getParent().getAbsoluteY() + scrollY - getAbsoluteY();
        if(position >= 0){
            if(previousPosition < 0){
                needToCheck = true;
            }
        }else{
            if(previousPosition > 0){
                needToCheck = true;
            }        
        }
        if (scrollY - oldscrollY >= 0) {
            if(needToCheck){
                pushToHeader();
            }
        }else{
            Stack stack = (Stack) getParent().getClientProperty("sticky");
            if (stack != null && !stack.empty() && stack.peek() == this && position<0) {
                popFromHeader();
            }
        }
        previousPosition = position;
        needToCheck = false;
    }

    void paintGlassImpl(Graphics g) {
    }

    private void popFromHeader() {
        Stack stack = (Stack) getParent().getClientProperty("sticky");        
        stack.pop();
        
        if(!stack.empty()){
            StickyHeader h = (StickyHeader)stack.peek();
            h.installSticky();
        }else{
            getComponentForm().setGlassPane(null);
        }
    }
    
    private void pushToHeader() {
        Stack stack = (Stack) getParent().getClientProperty("sticky");        
        if (stack == null) {            
            stack = new Stack();
            getParent().putClientProperty("sticky", stack);
        }
        if(!stack.empty()){
            StickyHeader h = (StickyHeader) stack.peek();
            if(getY() < h.getY()){
                return;
            }
        }
        stack.push(this);
        installSticky();
    }

    void installSticky() {
        Painter sticky = createPainter();
        getComponentForm().setGlassPane(sticky);
    }

    Painter createPainter() {
        Painter sticky = new Painter() {

            @Override
            public void paint(Graphics g, Rectangle rect) {
                int cx = g.getClipX();
                int cy = g.getClipY();
                int cw = g.getClipWidth();
                int ch = g.getClipHeight();

                int tx = getParent().getX() - getX();
                int ty = getParent().getY() - getY();
                g.setClip(0, 0, rect.getWidth(), rect.getHeight());

                g.translate(tx, ty);
                StickyHeader.this.paintComponentBackground(g);
                StickyHeader.this.paint(g);
                if (StickyHeader.this.getStyle().getBorder() != null) {
                    StickyHeader.this.paintBorder(g);
                }
                g.translate(-tx, -ty);
                g.setClip(cx, cy, cw, ch);
            }
        };
        return sticky;
    }

}
