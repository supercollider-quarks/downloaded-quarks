from scipy.sparse import dia_matrix
import numpy as np

bcs = ('AllSidesClamped','LeftClampedRightClampedTopClampedBottomSimplySupported',\
'LeftClampedRightClampedTopClampedBottomFree','LeftClampedRightClampedTopSimplySupportedBottomClamped',\
'LeftClampedRightClampedTopSimplySupportedBottomSimplySupported','LeftClampedRightClampedTopSimplySupportedBottomFree',\
'LeftClampedRightClampedTopFreeBottomClamped','LeftClampedRightClampedTopFreeBottomSimplySupported',\
'LeftClampedRightClampedTopFreeBottomFree','LeftClampedRightSimplySupportedTopClampedBottomClamped',\
'LeftClampedRightSimplySupportedTopClampedBottomSimplySupported','LeftClampedRightSimplySupportedTopClampedBottomFree',\
'LeftClampedRightSimplySupportedTopSimplySupportedBottomClamped','LeftClampedRightSimplySupportedTopSimplySupportedBottomSimplySupported',\
'LeftClampedRightSimplySupportedTopSimplySupportedBottomFree','LeftClampedRightSimplySupportedTopFreeBottomClamped',\
'LeftClampedRightSimplySupportedTopFreeBottomSimplySupported','LeftClampedRightSimplySupportedTopFreeBottomFree',\
'LeftClampedRightFreeTopClampedBottomClamped','LeftClampedRightFreeTopClampedBottomSimplySupported',\
'LeftClampedRightFreeTopClampedBottomFree','LeftClampedRightFreeTopSimplySupportedBottomClamped',\
'LeftClampedRightFreeTopSimplySupportedBottomSimplySupported','LeftClampedRightFreeTopSimplySupportedBottomFree',\
'LeftClampedRightFreeTopFreeBottomClamped','LeftClampedRightFreeTopFreeBottomSimplySupported',\
'LeftClampedRightFreeTopFreeBottomFree','LeftSimplySupportedRightClampedTopClampedBottomClamped',\
'LeftSimplySupportedRightClampedTopClampedBottomSimplySupported','LeftSimplySupportedRightClampedTopClampedBottomFree',\
'LeftSimplySupportedRightClampedTopSimplySupportedBottomClamped','LeftSimplySupportedRightClampedTopSimplySupportedBottomSimplySupported',\
'LeftSimplySupportedRightClampedTopSimplySupportedBottomFree','LeftSimplySupportedRightClampedTopFreeBottomClamped',\
'LeftSimplySupportedRightClampedTopFreeBottomSimplySupported','LeftSimplySupportedRightClampedTopFreeBottomFree',\
'LeftSimplySupportedRightSimplySupportedTopClampedBottomClamped','LeftSimplySupportedRightSimplySupportedTopClampedBottomSimplySupported',\
'LeftSimplySupportedRightSimplySupportedTopClampedBottomFree','LeftSimplySupportedRightSimplySupportedTopSimplySupportedBottomClamped',\
'AllSidesSimplySupported','LeftSimplySupportedRightSimplySupportedTopSimplySupportedBottomFree',\
'LeftSimplySupportedRightSimplySupportedTopFreeBottomClamped','LeftSimplySupportedRightSimplySupportedTopFreeBottomSimplySupported',\
'LeftSimplySupportedRightSimplySupportedTopFreeBottomFree','LeftSimplySupportedRightFreeTopClampedBottomClamped',\
'LeftSimplySupportedRightFreeTopClampedBottomSimplySupported','LeftSimplySupportedRightFreeTopClampedBottomFree',\
'LeftSimplySupportedRightFreeTopSimplySupportedBottomClamped','LeftSimplySupportedRightFreeTopSimplySupportedBottomSimplySupported',\
'LeftSimplySupportedRightFreeTopSimplySupportedBottomFree','LeftSimplySupportedRightFreeTopFreeBottomClamped',\
'LeftSimplySupportedRightFreeTopFreeBottomSimplySupported','LeftSimplySupportedRightFreeTopFreeBottomFree',\
'LeftFreeRightClampedTopClampedBottomClamped','LeftFreeRightClampedTopClampedBottomSimplySupported',\
'LeftFreeRightClampedTopClampedBottomFree','LeftFreeRightClampedTopSimplySupportedBottomClamped',\
'LeftFreeRightClampedTopSimplySupportedBottomSimplySupported','LeftFreeRightClampedTopSimplySupportedBottomFree',\
'LeftFreeRightClampedTopFreeBottomClamped','LeftFreeRightClampedTopFreeBottomSimplySupported',\
'LeftFreeRightClampedTopFreeBottomFree','LeftFreeRightSimplySupportedTopClampedBottomClamped',\
'LeftFreeRightSimplySupportedTopClampedBottomSimplySupported','LeftFreeRightSimplySupportedTopClampedBottomFree',\
'LeftFreeRightSimplySupportedTopSimplySupportedBottomClamped','LeftFreeRightSimplySupportedTopSimplySupportedBottomSimplySupported',\
'LeftFreeRightSimplySupportedTopSimplySupportedBottomFree','LeftFreeRightSimplySupportedTopFreeBottomClamped',\
'LeftFreeRightSimplySupportedTopFreeBottomSimplySupported','LeftFreeRightSimplySupportedTopFreeBottomFree',\
'LeftFreeRightFreeTopClampedBottomClamped','LeftFreeRightFreeTopClampedBottomSimplySupported',\
'LeftFreeRightFreeTopClampedBottomFree','LeftFreeRightFreeTopSimplySupportedBottomClamped',\
'LeftFreeRightFreeTopSimplySupportedBottomSimplySupported','LeftFreeRightFreeTopSimplySupportedBottomFree',\
'LeftFreeRightFreeTopFreeBottomClamped','LeftFreeRightFreeTopFreeBottomSimplySupported','AllSidesFree')

def checkInputArgs(Nx,Ny,bc):
    if not isinstance(Nx,int):
        raise TypeError('argument Nx must be of type int')
    elif Nx < 0:
        raise ValueError('argument Nx cannot be negative')
    if not isinstance(Ny,int):
        raise TypeError('argument Ny must be of type int')
    elif Ny < 0:
        raise ValueError('argument Ny cannot be negative')
    if not bc in bcs:
        raise ValueError('argument bc does not represent a valid boundary condition')

def laplacian_matrix_2d(Nx=3,Ny=3,bc='AllSidesClamped'):
    """
    generates the discrete laplacian operator in matrix form operating over a 2D grid of size (Nx - 1)*(Ny - 1)
    """
    checkInputArgs(Nx,Ny,bc)

    #np.set_printoptions(threshold=np.nan,linewidth=230,precision=2,suppress=True)

    if not 'Free' in bc:
        diag = 3*[None]
        diag[0] = -4 + np.zeros((Nx - 1)*(Ny - 1))
        diag[1] = np.ones((Nx - 1)*(Ny - 1))
        for i in xrange(1,Nx): diag[1][i*(Ny - 1) - 1] = 0
        diag[2] = np.ones((Nx - 1)*(Ny - 1))

        mat = dia_matrix(([diag[0],np.roll(diag[1],1),diag[1],diag[2],diag[2]],\
        [0,1,-1,Ny - 1,-Ny + 1]),shape=((Nx - 1)*(Ny - 1),(Nx - 1)*(Ny - 1)))
    else:
        raise NotImplementedError('free boundary conditions are not implemented yet')

    return mat

def biharmonic_matrix_2d(Nx=3,Ny=3,bc='AllSidesClamped'):
    """
    generates the discrete biharmonic operator in matrix form
    """
    checkInputArgs(Nx,Ny,bc)

    #np.set_printoptions(threshold=np.nan,linewidth=230,precision=2,suppress=True)

    diag = 6*[None]

    if bc == 'AllSidesClamped':
        diag[0] = 20 + np.zeros((Nx - 1)*(Ny - 1))
    elif bc == 'AllSidesSimplySupported':
        diag[0] = np.array([18] + (Ny - 3)*[19] + [18] + sum([[19] + (Ny - 3)*[20] + [19]]*(Nx - 3),[]) + [18] + (Ny - 3)*[19] + [18])
    elif bc == 'LeftClampedRightClampedTopClampedBottomSimplySupported':
        diag[0] = np.array(sum([[19] + [20]*(Ny - 2)]*(Nx - 1),[]))
    elif bc == 'LeftClampedRightClampedTopSimplySupportedBottomClamped':
        diag[0] = np.array(sum([[20]*(Ny - 2) + [19]]*(Nx - 1),[]))
    elif bc == 'LeftClampedRightClampedTopSimplySupportedBottomSimplySupported':
        diag[0] = np.array(sum([[19] + [20]*(Ny - 3) + [19]]*(Nx - 1),[]))
    elif bc == 'LeftClampedRightSimplySupportedTopClampedBottomClamped':
        diag[0] = [20]*(Ny - 1)*Nx + [19]*(Ny - 1)
    elif bc == 'LeftClampedRightSimplySupportedTopClampedBottomSimplySupported':
        diag[0] = sum([[19] + [20]*(Ny - 2)]*(Nx - 2),[]) + [18] + [19]*(Ny - 2)
    elif bc == 'LeftClampedRightSimplySupportedTopSimplySupportedBottomClamped':
        diag[0] = sum([[20]*(Ny - 2) + [19]]*(Nx - 2),[]) + [19]*(Ny - 2) + [18]
    elif bc == 'LeftClampedRightSimplySupportedTopSimplySupportedBottomSimplySupported':
        diag[0] = sum([[19] + [20]*(Ny - 3) + [19]]*(Nx - 2),[]) + [18] + [19]*(Ny - 3) + [18]
    elif bc == 'LeftSimplySupportedRightClampedTopClampedBottomClamped':
        diag[0] = [19]*(Ny - 1) + [20]*(Ny - 1)*(Nx - 2)
    elif bc == 'LeftSimplySupportedRightClampedTopClampedBottomSimplySupported':
        diag[0] = [18] + [19]*(Ny - 2) + sum([[19] + [20]*(Ny - 2)]*(Nx - 2),[])
    elif bc == 'LeftSimplySupportedRightClampedTopSimplySupportedBottomClamped':
        diag[0] = [19]*(Ny - 2) + [18] + sum([[20]*(Ny - 2) + [19]]*(Nx - 2),[])
    elif bc == 'LeftSimplySupportedRightClampedTopSimplySupportedBottomSimplySupported':
        diag[0] = [18] + [19]*(Ny - 3) + [18] + sum([[19] + [20]*(Ny - 3) + [19]]*(Nx - 2),[])
    elif bc == 'LeftSimplySupportedRightSimplySupportedTopClampedBottomClamped':
        diag[0] = [19]*(Ny - 1) + [20]*(Ny - 1)*(Nx - 3) + [19]*(Ny - 1)
    elif bc == 'LeftSimplySupportedRightSimplySupportedTopClampedBottomSimplySupported':
        diag[0] = [18] + [19]*(Ny - 2) + sum([[19] + [20]*(Ny - 2)]*(Nx - 3),[]) + [18] + [19]*(Ny - 2)
    elif bc == 'LeftSimplySupportedRightSimplySupportedTopSimplySupportedBottomClamped':
        diag[0] = [19]*(Ny - 2) + [18] + sum([[20]*(Ny - 2) + [19]]*(Nx - 3),[]) + [19]*(Ny - 2) + [18]
    else:
        raise NotImplementedError('free boundary conditions are not implemented yet')

    diag[1] = -8 + np.zeros((Nx - 1)*(Ny - 1))
    for i in xrange(1,Nx): diag[1][i*(Ny - 1) - 1] = 0
    diag[2] = np.ones((Nx - 1)*(Ny - 1))
    for i in xrange(1,Nx): diag[2][i*(Ny - 1) - 1] = 0; diag[2][i*(Ny - 1) - 2] = 0
    diag[3] = -8 + np.zeros((Nx - 1)*(Ny - 1))
    diag[4] = 2 + np.zeros((Nx - 1)*(Ny - 1))
    for i in xrange(1,Nx): diag[4][i*(Ny - 1) - 1] = 0
    diag[5] = np.ones((Nx - 1)*(Ny - 1))

    mat = dia_matrix(([diag[0],np.roll(diag[1],1),diag[1],np.roll(diag[2],2),diag[2],diag[3],diag[3],\
    np.roll(diag[4],1),np.roll(diag[4],Ny - 1),np.roll(diag[4],-Ny + 2),diag[4],diag[5],diag[5]],\
    [0,1,-1,2,-2,Ny - 1,-Ny + 1,Ny,Ny - 2,-Ny + 2,-Ny,2*(Ny - 1),2*(-Ny + 1)]),\
    shape=((Nx - 1)*(Ny - 1),(Nx - 1)*(Ny - 1)))

    return mat
