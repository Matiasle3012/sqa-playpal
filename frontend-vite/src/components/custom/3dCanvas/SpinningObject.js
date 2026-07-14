import { TOP_X, TOP_Y, BOTTOM_X, BOTTOM_Y } from "../3dCanvasBalls";

export class SpinningObject {
    dx = 0.1;
    dy = 0.1;
    dz = 0.1;
    rx = 0.025;
    ry = 0.025;
    rz = 0.025;
    timer = 0;
    sin_angle = 0;
    object = null;
    bounced = false;

    constructor(object, dx, dy, dz, rx, ry, rz) {
        this.object = object;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    updatePosition(mouse) {
        if ((TOP_Y < this.object.position.y || this.object.position.y < BOTTOM_Y) || (TOP_X < this.object.position.x || this.object.position.x < BOTTOM_X)) {
            this.object.position.x = 0;
            this.object.position.y = 0;
            
        }

        this.object.position.x += (mouse.x - this.object.position.x) * this.dx;
        this.object.position.y += (mouse.y - this.object.position.y) * this.dy;
        this.object.position.z += Math.sin(0.5 + this.sin_angle) * this.dz;
        this.sin_angle = this.sin_angle + 0.01 % 1.5;
        if (this.bounced) {
            if (this.timer >= 1) {
                this.dx *= -1;
                this.dy *= -1;
                this.dz *= -1;
                this.timer = 0.01;
                this.bounced = false;
            } else {
                this.timer *= this.timer;
            }
        }
    }
    updateRotation() {
        this.object.rotation.x += this.rx;
        this.object.rotation.y += this.ry;
        this.object.rotation.z += this.rz;
    }
    checkCollision(other, mouse) {
        const Dab = Math.sqrt(
            Math.pow((this.object.position.x + (mouse.x - this.object.position.x) * this.dx) - (other.object.position.x + (mouse.x - other.object.position.x) * other.dx), 2) 
            + Math.pow((this.object.position.y + (mouse.y - this.object.position.y) * this.dy) - (other.object.position.y + (mouse.y - other.object.position.y) * other.dy), 2) 
            + Math.pow((this.object.position.z + Math.sin(0.5 + this.sin_angle) * this.dz) - (other.object.position.z + Math.sin(0.5 + other.sin_angle) * other.dz), 2) 
    );
        if ((this.object.scale.x + other.object.scale.y) > Dab) {
            return true;
        }
        return false;
    }
}