## Should have run: 
# !pip install pygame
# !pip install pymunk 

import random
import time
import pygame
import numpy
from pygame.locals import *
from pygame.color import *
import pymunk
from pymunk import Vec2d
import pymunk.pygame_util

# Some general variables -- you don't need to change any of these
N_BLOCKS = 6 # How many blocks will fall?
BLOCK_SIZE = 20 # How big are the blocks?
deltaY     = 35 # How far spaced out vertically are they?
xSD        = 30.0 # What is the SD for their x-locations?
FPS = 30. # how many frames per second do we run?
BLOCK_MASS = 1.0
BLOCK_FRICTION = 1.0
FLOOR = 100
RUN_TIME = 20.0 # Time in seconds that we will run a simulation for -- this was increased to be sure we run for long enough across platforms
STEPS_PER_FRAME = 5.0 # Do not change this
WIDTH = 600 # Screen dimensions -- don't change
HEIGHT = 600

class BlockTower:
    # Implement a class to show/simulate blocks falling via pymunk
    # Note: this code has been modified from the pymunk pyramid demo

    def __init__(self, positions):
        # The intializer takes a list of x-positions for blocks; their height is set
        # by the code here.
        assert(len(positions)==N_BLOCKS) # can't give more than N_BLOCKS since we need to draw them

        self.positions = positions # store the positions of our blocks

        # Set up some pygame stuff
        self.running = True
        self.physics_running = False
        self.start_time = 0
        self.drawing = True
        self.w, self.h = WIDTH,HEIGHT
        self.screen = pygame.display.set_mode((self.w, self.h))
        self.clock = pygame.time.Clock()

        ### Init pymunk and create space
        self.space = pymunk.Space()
        self.space.gravity = (0.0, -900.0)
        self.space.sleep_time_threshold = 0.3

        self.floor = pymunk.Segment(self.space.static_body, (0, FLOOR), (self.w,FLOOR), 1.0)
        self.floor.friction = 1.0
        self.space.add(self.floor)

        # Draw each block and add it to the physics
        for i in range(N_BLOCKS):
            points = [(-BLOCK_SIZE, -BLOCK_SIZE), (-BLOCK_SIZE, BLOCK_SIZE), (BLOCK_SIZE,BLOCK_SIZE), (BLOCK_SIZE, -BLOCK_SIZE)]
            moment = pymunk.moment_for_poly(BLOCK_MASS, points, (0,0))
            body = pymunk.Body(BLOCK_MASS, moment)
            xpos = self.positions[i]
            ypos = FLOOR + (2*i+1) * deltaY
            body.position = Vec2d(xpos,ypos)
            shape = pymunk.Poly(body, points)
            if(i == N_BLOCKS-1):     # color the top
                shape.color = (1,0,0,1)
                self.target_block = shape # store the top one we are tracking
            shape.friction = 1
            self.space.add(body,shape)

        ### draw options for drawing
        self.draw_options = pymunk.pygame_util.DrawOptions(self.screen)
        
    def change_x_y(self, new_x):
        # This is what you should call instead of initializing new objects
        # this resets the y-positions and puts the blocks at the specified x positions
        self.space.remove(self.space.bodies)
        self.__init__(new_x)

    def is_black_block_on_floor(self):
        # Returns true or false depending on whether the black block is on the bottom
        col = self.target_block.shapes_collide(self.floor) # this resturns a ContactPointSet
        return len(col.points) > 0


    def simulate(self):
        # Just run a simulation, returning whether after 10s the black block hits the floor
        for s in range(int(FPS*5*RUN_TIME)): # run for 10s
            self.space.step(1.0 / FPS / STEPS_PER_FRAME) # run this many steps
        return self.is_black_block_on_floor()

    def draw(self, file="screenshot.jpg"):
        ### This gets called to draw the scene

        ### Clear the screen
        self.screen.fill(THECOLORS["white"])

        ### Draw space  with our given options
        self.space.debug_draw(self.draw_options)

        ## Save to a file
        pygame.image.save(self.screen, file)


if __name__ == "__main__":
    # Set up a null pygame video driver
    import os
    os.environ['SDL_VIDEODRIVER']='dummy'

    from IPython.display import Image # required for showing the image


    positions = [numpy.random.normal(WIDTH/2, xSD) for _ in range(N_BLOCKS)]

    # set up a block tower
    demo2 = BlockTower(positions) # make a block tower
    demo2.draw() # draw it -- this saves it as a file called screenshot.jpg, defaultly

    # show the image
    display(Image(filename='screenshot.jpg') )

    # gather input (should be yes/no)
    guess = input()

    # print the simulation result
    print(demo2.simulate())
