package com.mytestgame.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture playerTexture;
    private Sprite player;

    private Texture enemyTexture;
    private List<Enemy> enemies;

    private Texture bulletTexture;
    private List<Bullet> bullets;

    private int x = 0;
    private int y = 0;

    private float speed = 100f;
    private float enemySpeed = 20f;

    private boolean isReloading = false;
    private float reloadTime = 0f;
    private float shootCooldown = 0.1f;
    private float timeSinceLastShot = 0f;
    private int bulletsInBurst = 1;

    private float enemyShootCooldown = 2f;
    private float timeSinceEnemyLastShot = 0f;

    private int playerHealth = 100;

    Random rand = new Random();

    @Override
    public void create() {
        batch = new SpriteBatch();
        playerTexture = new Texture("Player.png");
        player = new Sprite(playerTexture);
        player.setPosition(20, 30);
        player.setOriginCenter();

        enemyTexture = new Texture("Enemy.png");
        enemies = new ArrayList<>();
        createEnemies(3);

        bulletTexture = new Texture("bullet.png");
        bullets = new ArrayList<>();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            float touchX = Gdx.input.getX();
            float screenMiddle = (float) Gdx.graphics.getWidth() / 2;

            if (touchX < screenMiddle) {
                player.translateX(-speed * delta);
                player.setRotation(180);
            } else {
                player.translateX(speed * delta);
                player.setRotation(0);
            }

            float playerX = player.getX();
            if (playerX < 0) {
                player.setX(0);
            } else if (playerX + player.getWidth() > Gdx.graphics.getWidth()) {
                player.setX(Gdx.graphics.getWidth() - player.getWidth());
            }
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !isReloading) {
            if (timeSinceLastShot >= shootCooldown) {
                shootBurst();
                timeSinceLastShot = 0f;
            }
        }

        timeSinceLastShot += delta;

        if (isReloading) {
            reloadTime += delta;
            if (reloadTime >= 1f) {
                isReloading = false;
                reloadTime = 0f;
            }
        }

        moveBullets(delta);
        moveEnemies(delta);

        timeSinceEnemyLastShot += delta;
        if (timeSinceEnemyLastShot >= enemyShootCooldown) {
            shootEnemyBullets();
            timeSinceEnemyLastShot = 0f;
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        batch.begin();
        player.draw(batch);
        for (Enemy enemy : enemies) {
            enemy.sprite.draw(batch);
            System.out.println("enemy hp" + enemy.health);
        }
        System.out.println("playerHealth" + playerHealth);
        for (Bullet bullet : bullets) {
            bullet.sprite.draw(batch);
        }

        batch.end();
    }

    private void shootBurst() {
        for (int i = 0; i < bulletsInBurst; i++) {
            Sprite newBullet = new Sprite(bulletTexture);
            newBullet.setScale(0.1f);
            float bulletX = player.getX() + player.getWidth() / 2 - newBullet.getWidth() / 2;
            float bulletY = 5;


            System.out.println("Bullet Position: X=" + bulletX + ", Y=" + bulletY);


            newBullet.setPosition(bulletX, bulletY);
//            newBullet.setPosition(player.getX() + player.getWidth() / 2 - newBullet.getWidth() / 2, 5);
            bullets.add(new Bullet(newBullet, true));
        }

        isReloading = true;
    }

    private void moveBullets(float delta) {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            if (bullet.isFromPlayer) {
                bullet.sprite.translateY(300 * delta);
            } else {
                bullet.sprite.translateY(-300 * delta);
            }

            if (bullet.sprite.getY() + bullet.sprite.getHeight() < 0 || bullet.sprite.getY() > Gdx.graphics.getHeight()) {
                bulletsToRemove.add(bullet);
            }

            for (Enemy enemy : enemies) {
                if (bullet.isFromPlayer && bullet.sprite.getBoundingRectangle().overlaps(enemy.sprite.getBoundingRectangle())) {
                    if (enemy.health > 0) {
                        enemy.health--;
                        bulletsToRemove.add(bullet);
                    }

                    if (enemy.health <= 0) {
                        respawnEnemy(enemy);
                    }
                }
            }

            if (!bullet.isFromPlayer && bullet.sprite.getBoundingRectangle().overlaps(player.getBoundingRectangle())) {
                if (playerHealth > 0) {
                    playerHealth--;
                    bulletsToRemove.add(bullet);
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
    }


    private void moveEnemies(float delta) {
        for (Enemy enemy : enemies) {

            float enemyX = enemy.sprite.getX() + enemy.sprite.getWidth() / 2;
            float enemyY = enemy.sprite.getY() + enemy.sprite.getHeight() / 2;

            float playerX = player.getX() + player.getWidth() / 2;
            float playerY = player.getY() + player.getHeight() / 2;


            float deltaX = playerX - enemyX;
            float deltaY = playerY - enemyY;


            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance != 0) {
                deltaX /= distance;
                deltaY /= distance;
            }


            enemy.sprite.translateX(deltaX * enemySpeed * delta);
            enemy.sprite.translateY(deltaY * enemySpeed * delta);


            if (enemy.sprite.getX() < 0) {
                enemy.sprite.setX(0);
            } else if (enemy.sprite.getX() + enemy.sprite.getWidth() > Gdx.graphics.getWidth()) {
                enemy.sprite.setX(Gdx.graphics.getWidth() - enemy.sprite.getWidth());
            }

            if (enemy.sprite.getY() < 0) {
                enemy.sprite.setY(0);
            } else if (enemy.sprite.getY() + enemy.sprite.getHeight() > Gdx.graphics.getHeight()) {
                enemy.sprite.setY(Gdx.graphics.getHeight() - enemy.sprite.getHeight());
            }

            enemy.sprite.translateY(-enemySpeed * delta);
            if (enemy.sprite.getY() <= 5) {
                respawnEnemy(enemy);
            }
        }
    }

    private void createEnemies(int count) {
        for (int i = 0; i < count; i++) {
            Sprite newEnemySprite = new Sprite(enemyTexture);
            newEnemySprite.setScale(0.7f);
            newEnemySprite.setOriginCenter();
            newEnemySprite.setPosition(rand.nextFloat() * (Gdx.graphics.getWidth() - newEnemySprite.getWidth()), Gdx.graphics.getHeight());
            enemies.add(new Enemy(newEnemySprite, 3));
        }
    }

    private void respawnEnemy(Enemy enemy) {
        float randomX = rand.nextFloat() * (Gdx.graphics.getWidth() - enemy.sprite.getWidth());
        float randomY = Gdx.graphics.getHeight();
        enemy.sprite.setPosition(randomX, randomY);
        enemy.health = 3;
    }

    private void shootEnemyBullets() {
        for (Enemy enemy : enemies) {
            Sprite bullet = new Sprite(bulletTexture);
            bullet.setScale(0.1f);
            float enemyX = enemy.sprite.getX() + enemy.sprite.getWidth() / 2;
            float enemyY = enemy.sprite.getY() - enemy.sprite.getHeight();

            float deltaX = player.getX() - enemyX;
            float deltaY = player.getY() - enemyY;

            bullet.setPosition(enemyX, enemyY);

            bullets.add(new Bullet(bullet, false));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        enemyTexture.dispose();
        bulletTexture.dispose();
    }

    private static class Enemy {
        Sprite sprite;
        int health;

        public Enemy(Sprite sprite, int health) {
            this.sprite = sprite;
            this.health = health;
        }

        public int getHealth() {
            return health;
        }
    }

    private static class Bullet {
        Sprite sprite;
        boolean isFromPlayer;

        public Bullet(Sprite sprite, boolean isFromPlayer) {
            this.sprite = sprite;
            this.isFromPlayer = isFromPlayer;
        }
    }
}
