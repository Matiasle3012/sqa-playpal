import { React, useRef, useEffect } from 'react';
import * as THREE from 'three';
import { SpinningObject } from './3dCanvas/SpinningObject';

const TOP_Y = 7;
const TOP_X = 7;
const BOTTOM_Y = -7;
const BOTTOM_X = -7;

export { TOP_X, TOP_Y, BOTTOM_X, BOTTOM_Y };

function updateCollisions(balls, mouse) {
    for (const ball of balls) {
        for (const otherBall of balls) {
            if (ball.object !== otherBall.object && ball.checkCollision(otherBall, mouse)) {
                ball.dx *= -1;
                ball.dy *= -1;
                ball.dz *= -1;
                ball.bounced = true;
                // otherBall.dx *= -1;
                // otherBall.dy *= -1;
                // otherBall.dz *= -1;
                return true;
            }
        }
    }
    return false;
}

function create_ball() {
    const urls = [
        'https://th.bing.com/th/id/OIP.l9iSdVYpB2qND3PCXiSRzgHaE8?w=292&h=195&c=7&r=0&o=5&dpr=1.3&pid=1.7',
        'https://th.bing.com/th/id/OIP.pOl_uC5JGYeW4B3m8DIo5QHaFL?w=298&h=209&c=8&rs=1&qlt=90&o=6&dpr=1.3&pid=3.1&rm=2',
        'https://th.bing.com/th/id/OIP.i0h_ImnfKs4klVqI1cGu1wHaHH?rs=1&pid=ImgDetMain'
    ]

    const sphere_geometry = new THREE.SphereGeometry();
    const sphere_texture = new THREE.TextureLoader().load( urls[Math.floor(Math.random() * urls.length)]);
    
    const sphere_material = new THREE.MeshPhongMaterial({ map: sphere_texture });
    const sphere = new THREE.Mesh(sphere_geometry, sphere_material);
    sphere.position.x = (Math.random() - 0.5) * 5;
    sphere.position.y = (Math.random() - 0.5) * 5;
    const scale = (Math.random() + 0.5) * 0.25;
    sphere.scale.set(scale, scale, scale);

    return new SpinningObject(sphere, Math.random() * 0.01, Math.random() * 0.01, (Math.random() - 0.5) * 0.01, (Math.random() - 0.5) * 0.025, (Math.random() - 0.5) * 0.025, (Math.random() - 0.5) * 0.025);
}

export default function BgAnimation() {
    let objects = [];

    const scene = new THREE.Scene();
    scene.background = new THREE.Color(0xfdf9f1);
    const aspectratio = window.innerWidth / window.innerHeight;
    const camera = new THREE.PerspectiveCamera(75, aspectratio, 0.1, 1000);
    const light = new THREE.DirectionalLight(0xfdf9f1, 3);
    light.position.set(0, 0, 3);
    light.target.position.set(0, 0, 0);
    scene.add(light);
    scene.add(light.target);

    const renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);

    const mountRef = useRef(null);

    for (let i = 0; i < 5; i++) {
        const object = create_ball();
        objects.push(object);
        scene.add(object.object);
    }

    useEffect(() => {

        if (mountRef.current) {
            mountRef.current.appendChild(renderer.domElement);


            camera.position.z = 5;

            const mouse = { x: 0, y: 0 };
            let sin_angle = 0;

            window.addEventListener('mousemove', (event) => {
                mouse.x = (event.clientX / window.innerWidth * 20 * aspectratio) - (10 * aspectratio);
                mouse.y = -(event.clientY / window.innerHeight * 20) + 10;
            });

            const handleResize = () => {
                const newAspectRatio = window.innerWidth / window.innerHeight;
                camera.aspect = newAspectRatio;
                camera.updateProjectionMatrix();
                renderer.setSize(window.innerWidth, window.innerHeight);
            };

            window.addEventListener('resize', handleResize);

            const animate = () => {
                requestAnimationFrame(animate);
                
                // oscilar entre PI / 2 y PI * 3/2
                sin_angle = sin_angle + 0.01 % 1.5;
                
                for (let object of objects) {
                    updateCollisions(objects, mouse);
                    object.updatePosition(mouse);
                    object.updateRotation();
                }

                renderer.render(scene, camera);
            };

            animate();

            return () => {
                if (mountRef.current) {
                    mountRef.current.removeChild(renderer.domElement);
                }
                window.removeEventListener('resize', handleResize);
            };
        }
    }, []);

    return (
        <div ref={mountRef} style={{ width: '100vw', height: '100vh' }}></div>
    );
}