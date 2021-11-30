package org.csc133.a3.commands;

import com.codename1.ui.Command;
import com.codename1.ui.events.ActionEvent;
import org.csc133.a3.GameWorld;

public class Start extends Command{
    private GameWorld gw;

    public Start(GameWorld gw){
        super("Start");
        this.gw =gw;
    }

    @Override
    public void actionPerformed(ActionEvent e){ gw.Start(); }
}
