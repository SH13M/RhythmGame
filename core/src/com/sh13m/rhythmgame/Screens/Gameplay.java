package com.sh13m.rhythmgame.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sh13m.rhythmgame.RhythmGame;
import com.sh13m.rhythmgame.Tools.SongReader;
import com.sh13m.rhythmgame.Tools.TextUtil;

import java.util.Iterator;

public class Gameplay implements Screen {
    // gameplay settings
    private static final int R_HEIGHT = 30;
    private static final int C_HEIGHT = 300;
    private static final int SCROLL_SPEED = 900;
    private static final float GLOBAL_DELAY = 3;
    private static final float SCROLL_OFFSET = (480f - R_HEIGHT) / SCROLL_SPEED;

    // render
    private final RhythmGame game;
    private final Viewport viewport;
    private final OrthographicCamera cam;
    private final Pixmap pm;
    private final Cursor cursor;

    // textures
    private final Texture note_img;
    private final TextureRegion receptors_img;
    private final TextureRegion note_1;
    private final TextureRegion note_2;
    private final TextureRegion note_3;
    private final TextureRegion note_4;
    private final TextureRegion note_clicked_1;
    private final TextureRegion note_clicked_2;
    private final TextureRegion note_clicked_3;
    private final TextureRegion note_clicked_4;
    private final Texture stage;

    // rectangles
    private Rectangle receptor1;
    private Rectangle receptor2;
    private Rectangle receptor3;
    private Rectangle receptor4;

    // song data
    private Music music;
    private SongReader sr;
    private boolean START = false;

    // temp other
    private int combo;


    public Gameplay(RhythmGame game) {
        this.game = game;
        // sets the cursor invisible
        pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        cursor = Gdx.graphics.newCursor(pm,0,0);
        Gdx.graphics.setCursor(cursor);

        // set up camera
        cam = new OrthographicCamera();
        cam.setToOrtho(false, RhythmGame.V_WIDTH, RhythmGame.V_HEIGHT);
        viewport = new FitViewport(RhythmGame.V_WIDTH, RhythmGame.V_HEIGHT);

        // set up textures
        note_img = new Texture(Gdx.files.internal("Graphics/notes.png"));
        receptors_img = new TextureRegion(note_img,0,64,256,64);
        note_1 = new TextureRegion(note_img, 0,0,64,64);
        note_2 = new TextureRegion(note_img, 64,0,64,64);
        note_3 = new TextureRegion(note_img, 128,0,64,64);
        note_4 = new TextureRegion(note_img,192,0,64,64);
        note_clicked_1 = new TextureRegion(note_img, 0,128,64,64);
        note_clicked_2 = new TextureRegion(note_img, 64,128,64,64);
        note_clicked_3 = new TextureRegion(note_img, 128,128,64,64);
        note_clicked_4 = new TextureRegion(note_img,192,128,64,64);
        stage = new Texture(Gdx.files.internal("Graphics/stage.png"));

        // set up rectangles
        receptor1 = new Rectangle(RhythmGame.V_WIDTH / 2 - 128, R_HEIGHT,64,64);
        receptor2 = new Rectangle(RhythmGame.V_WIDTH / 2 - 64, R_HEIGHT,64,64);
        receptor3 = new Rectangle(RhythmGame.V_WIDTH / 2, R_HEIGHT,64,64);
        receptor4 = new Rectangle(RhythmGame.V_WIDTH / 2 + 64, R_HEIGHT,64,64);

        // set up song data
        music = Gdx.audio.newMusic(Gdx.files.internal("Songs/death waltz (Wh1teh)/audio.mp3"));
        sr = new SongReader();
        Timer delayedMusicStart = new Timer();
        delayedMusicStart.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                music.play();
            }
        }, GLOBAL_DELAY + sr.offset);
        Timer delayedNoteStart = new Timer();
        delayedNoteStart.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                START = true;
            }
        }, GLOBAL_DELAY - SCROLL_OFFSET);

        // temp other set up
        combo = 0;
    }

    @Override
    public void show() {
        game.font.getData().setScale(0.5f);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        game.batch.draw(stage, RhythmGame.V_WIDTH / 2 - stage.getWidth() / 2, 0);
        game.batch.draw(receptors_img, RhythmGame.V_WIDTH / 2 - receptors_img.getRegionWidth() / 2 , R_HEIGHT);
        drawInput();
        drawNotes();
        game.font.draw(game.batch, String.valueOf(combo), RhythmGame.V_WIDTH / 2 - TextUtil.getTextWidth(game.font, String.valueOf(combo)) / 2, C_HEIGHT);
        game.ltext.draw(game.batch, "GAMEPLAY", 5, 20);
        game.batch.end();
    }

    private void update(float delta) {
        handleInput();
        if (!sr.song_ended && START) sr.readMeasure(delta);
        updateNotes();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new SongSelect(game));
            dispose();
        }

    }

    private void drawInput() {
        // lights up receptors if keys are pressed
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            game.batch.draw(note_clicked_1, RhythmGame.V_WIDTH / 2 - 128, R_HEIGHT);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.F) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            game.batch.draw(note_clicked_2, RhythmGame.V_WIDTH / 2 - 64, R_HEIGHT);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.J) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            game.batch.draw(note_clicked_3, RhythmGame.V_WIDTH / 2, R_HEIGHT);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.K) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            game.batch.draw(note_clicked_4, RhythmGame.V_WIDTH / 2 + 64, R_HEIGHT);
        }
    }

    private void updateNotes() {
        for (Iterator<Rectangle> iter = sr.notes_col_1.iterator(); iter.hasNext(); ) {
            Rectangle note = iter.next();
            note.y -= SCROLL_SPEED * Gdx.graphics.getDeltaTime();
            if (note.y + 64 < 0) {
                iter.remove();
                combo = 0;
            }
            if (note.overlaps(receptor1) && Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                iter.remove();
                combo++;
            }
        }
        for (Iterator<Rectangle> iter = sr.notes_col_2.iterator(); iter.hasNext(); ) {
            Rectangle note = iter.next();
            note.y -= SCROLL_SPEED * Gdx.graphics.getDeltaTime();
            if (note.y + 64 < 0) {
                iter.remove();
                combo = 0;
            }
            if (note.overlaps(receptor2) && Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                iter.remove();
                combo++;
            }
        }
        for (Iterator<Rectangle> iter = sr.notes_col_3.iterator(); iter.hasNext(); ) {
            Rectangle note = iter.next();
            note.y -= SCROLL_SPEED * Gdx.graphics.getDeltaTime();
            if (note.y + 64 < 0) {
                iter.remove();
                combo = 0;
            }
            if (note.overlaps(receptor3) && Gdx.input.isKeyJustPressed(Input.Keys.J)) {
                iter.remove();
                combo++;
            }
        }
        for (Iterator<Rectangle> iter = sr.notes_col_4.iterator(); iter.hasNext(); ) {
            Rectangle note = iter.next();
            note.y -= SCROLL_SPEED * Gdx.graphics.getDeltaTime();
            if (note.y + 64 < 0) {
                iter.remove();
                combo = 0;
            }
            if (note.overlaps(receptor4) && Gdx.input.isKeyJustPressed(Input.Keys.K)) {
                iter.remove();
                combo++;
            }
        }
    }

    private void drawNotes() {
        for (Rectangle note : sr.notes_col_1) {
            game.batch.draw(note_1, note.x, note.y);
        }
        for (Rectangle note : sr.notes_col_2) {
            game.batch.draw(note_2, note.x, note.y);
        }
        for (Rectangle note : sr.notes_col_3) {
            game.batch.draw(note_3, note.x, note.y);
        }
        for (Rectangle note : sr.notes_col_4) {
            game.batch.draw(note_4, note.x, note.y);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        note_img.dispose();
        pm.dispose();
        cursor.dispose();
        music.dispose();
        stage.dispose();
    }
}
