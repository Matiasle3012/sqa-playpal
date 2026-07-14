export class Ball {
  ballX;
  aballY;
  velX;
  velY;
  G;
  W;

  constructor(x, y, vx, vy, g, w) {
    this.ballX = x;
    this.ballY = y;
    this.velX = vx;
    this.velY = vy;
    this.G = g;
    this.W = w;
  };

  update(mH, mW, scale) {
    let pred = this.ballX + this.velX;
    if (this.W <= pred && pred < mW - this.W) {
      this.ballX += this.velX;
    } else {
        this.velX *= -1;
    };
    
    pred = this.ballY + this.velY;
    if (this.W * scale <= pred && pred < mH - this.W * scale) {
        this.velY += this.G;
        this.ballY += this.velY * scale;
    } else {
        this.velY *= -1;
    }
  };

  draw(ctx) {
    ctx.beginPath()
    ctx.arc(this.ballX, this.ballY, this.W, 0, 2*Math.PI)
    ctx.fill()
  };
}