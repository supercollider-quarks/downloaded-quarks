
Camera2D : Vehicle {

  classvar <>fwd, <>back, <>rotLeft, <>rotRight, <>instance;
  var <>arrive, <>rotation, <>friction = 0.6,
  >motionAmount = 1.6, >rotationAmount= 0.01pi;


  *new{ arg world, position= RealVector2D[15,15], radius = 0.2, mass = 0.05,
        velocity = RealVector2D[0, 0], collisionType = \mobile, heading,
        side, maxSpeed = 83, maxForce = 10, maxTurnRate = 2;

      if(instance.isNil,
        {
          ^super.new(world,
            position,
            radius,
            mass
          ).velocity_(velocity)
          .collisionType_(collisionType)
          .heading_(heading)
          .side_(side)
          .maxSpeed_(maxSpeed)
          .maxForce_(maxForce)
          .maxTurnRate_(maxTurnRate);
        },
        {"There is already an active instance of Camera2D".error;}
      );
  }

  init{
    super.init;
    this.setCollisionResolution;
    instance = this;
    arrive = Arrive(this, position);
    this.force_({arg ent; arrive.value});
    rotation = 0;
    this.collisionFunc_({arg entity, entList, additionalInfo;
      entList.do{ arg obstacle;
        case
        {obstacle.isKindOf(Wall)} {NonPenetrationConstrainWall(entity, obstacle, 0.1)};
      }
    });
  }

  /* public */

  *active{
    if(instance.isNil,
      {^false},
      {^true}
    );
  }

  remove { arg confirm = false;
    if (confirm)
    {
      super.remove;
      instance = nil;
    };
  }

  applyTransformation{ arg entity;
      if (entity != this,
        {^this.translatePosition(entity.position)},
        {^entity.position}
      );
  }

  moveFwd{var theta, x, y;
    theta = rotation;
    y = theta.cos;
    x = theta.sin;
    arrive.targetPos = arrive.targetPos + (motionAmount *RealVector2D[x, y]);
  }

  moveBack{var theta, x, y;
    theta = rotation;
    y = theta.cos;
    x = theta.sin;
    arrive.targetPos = arrive.targetPos - (motionAmount *RealVector2D[x, y]);
  }

  forceFwd{ var theta, x, y;
    theta = rotation;
    y = theta.cos;
    x = theta.sin;
    arrive.targetPos = (motionAmount *RealVector2D[x, y]);
    this.velocity = arrive.targetPos;
    this.force_({arg entity; entity.velocity = entity.velocity * friction; 0});
  }

  forceBack{var theta, x, y;
    theta = rotation;
    y = theta.cos;
    x = theta.sin;
    arrive.targetPos = -1 * (motionAmount * RealVector2D[x, y]);
    this.velocity = arrive.targetPos;
    this.force_({arg entity; entity.velocity = entity.velocity * friction; 0})
  }

  rotateLeft{
    rotation = (rotation - rotationAmount).wrap(0, 2pi);
  }

  rotateRight{
    rotation = (rotation + rotationAmount).wrap(0, 2pi);
  }

 goto{ arg target;
    arrive.targetPos = target;
    this.force_({arg ent; arrive.value});
  }

  reset{
    arrive.targetPos = world.center;
    rotation = 0;
  }

  fastReset{
    position = world.center;
  }

  /* private */

  setCollisionResolution{
    collisionFunc = {};
    /* { arg entity, entList; */
    /*  CollisionResolution.nonPenetrationConstrain(entity, entList, 1.6); */
    /* }; */
  }

  translatePosition{ arg entPos;
    var thetaSin, thetaCos, rad, x,y;
    var xMinusx, yMinusy;
    thetaSin = rotation.sin;
    thetaCos = rotation.cos;
    xMinusx = entPos[0] - position[0];
    yMinusy = entPos[1] - position[1];
    x = (xMinusx * thetaCos) - (yMinusy * thetaSin);
    y = (xMinusx * thetaSin) + (yMinusy * thetaCos);
    ^RealVector2D[x,y];
  }

  //hack only for when we are at the middle of a side of the space (within a metre) with 0 rotation
  //TODO: fix the rotation with vectors to be able to use proper math
  rotateTowardsCenter{ var halfOfXSide, halfOfYSide;
    halfOfXSide = world.sceneWidth*0.5;
    halfOfYSide = world.sceneHeight*0.5;
    case
    {( position.x < (halfOfXSide + 0.5) ) && ( position.x > (halfOfXSide - 0.5) ) && ( position.y < 0.5) } {rotation = 0}
    {( position.x < (halfOfXSide + 0.5) ) && ( position.x > (halfOfXSide - 0.5) ) && ( position.y > (world.sceneHeight - 0.5)) } {rotation = 3.14}
    {( position.y < (halfOfYSide + 0.5) ) && ( position.y > (halfOfYSide - 0.5) ) && ( position.x < 0.5) } {rotation = 1.57}
    {( position.y < (halfOfYSide + 0.5) ) && ( position.y > (halfOfYSide - 0.5) ) && ( position.x > (world.sceneWidth - 0.5)) } {rotation = 4.71};
                                    

    /* var angle, normHeading; */ 
    /* normHeading = this.heading.normalize; */
    /* angle = atan2(point.y - normHeading.y, point.x - normHeading.x ); */
    /* angle.debug("place 1"); */
    /* /1* angle = angle * (180/pi); *1/ */
    /* /1* angle.debug("place 2"); *1/ */
    /* this.rotation = angle; */
  }

}

Camera2DRepresentation : VisualRepresentation{

  position{
    ^entity.worldCenter;
  }

}
