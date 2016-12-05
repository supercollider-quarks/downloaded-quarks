from scipy.sparse import dia_matrix
import numpy as np

bcs = ('BothClamped','LeftClampedRightSimplySupported','LeftSimplySupportedRightClamped',\
'BothSimplySupported','LeftClampedRightFree','LeftFreeRightClamped','LeftSimplySupportedRightFree',\
'LeftFreeRightSimplySupported','BothFree')

def checkInputArgs(N,bc):
    if not isinstance(N,int):
        raise TypeError('argument N must be of type int')
    elif N < 0:
        raise ValueError('argument N cannot be negative')
    if not bc in bcs:
        raise ValueError('argument bc does not represent a valid boundary condition')

def second_difference_matrix(N=3,bc='BothClamped'):
    """
    generates the second order difference operator in matrix form
    """
    checkInputArgs(N,bc)

    diag = 2*[None]

    if bc == 'BothFree':
        M = N + 1
    elif 'Free' in bc:
        M = N
    else:
        M = N - 1

    if 'Free' not in bc:
        diag[0] = -2 + np.zeros(M)
        diag[1] = np.ones(M)
        mat = dia_matrix(([diag[0],diag[1],diag[1]],[0,1,-1]),shape=(M,M))
    else:
        if bc == 'BothFree':
            diag[0] = np.concatenate((np.zeros(1),-2 + np.zeros(N - 1),np.zeros(1)))
            diag[1] = np.concatenate((np.ones(N - 1),np.zeros(2)))
            mat = dia_matrix(([diag[0],np.roll(diag[1],2),diag[1]],[0,1,-1]),shape=(M,M))
        elif bc in ('LeftClampedRightFree','LeftSimplySupportedRightFree'):
            diag[0] = np.concatenate((-2 + np.zeros(N - 1),np.zeros(1)))
            diag[1] = np.concatenate((np.ones(N - 1),np.zeros(1)))
            mat = dia_matrix(([diag[0],np.roll(diag[1],1),np.roll(diag[1],-1)],[0,1,-1]),shape=(M,M))
        elif bc in ('LeftFreeRightClamped','LeftFreeRightSimplySupported'):
            diag[0] = np.concatenate((np.zeros(1),-2 + np.zeros(N - 1)))
            diag[1] = np.concatenate((np.zeros(1),np.ones(N - 1)))
            mat = dia_matrix(([diag[0],np.roll(diag[1],1),np.roll(diag[1],-1)],[0,1,-1]),shape=(M,M))

    return mat

def fourth_difference_matrix(N=3,bc='BothClamped',cfs=None):
    """
    generates the fourth order difference operator in matrix form
    """
    checkInputArgs(N,bc)

    diag = 3*[None]

    # determine matrix dimensions based on N and type of boundary conditions
    if bc == 'BothFree':
        M = N + 1
    elif 'Free' in bc:
        M = N
    else:
        M = N - 1

    diag[0] = 6 + np.zeros(M)
    diag[1] = -4 + np.zeros(M)
    diag[2] = np.ones(M)

    if 'Free' not in bc:
        if bc == 'BothSimplySupported':
            diag[0][0] = 5; diag[0][-1] = 5
        elif bc == 'LeftSimplySupportedRightClamped':
            diag[0][0] = 5
        elif bc == 'LeftClampedRightSimplySupported':
            diag[0][-1] = 5

        return dia_matrix(([diag[0],diag[1],diag[1],diag[2],diag[2]],[0,1,-1,2,-2]),shape=(M,M))
    else:
        if cfs == None:
            raise TypeError('argument cfs must be supplied')
        elif not isinstance(cfs,dict):
            raise TypeError('argument cfs must be of type dict')

        if bc == 'BothFree':
            diag[0][0] = cfs['a0']; diag[0][-1] = cfs['a0']
            diag[0][1] = 5; diag[0][-2] = 5
            diag[1][0] = -2; diag[1][-2] = cfs['a1']
            diag2 = np.concatenate((cfs['a2'] + np.zeros(1),np.zeros(M - 2),cfs['a2'] + np.zeros(1)))
            Dxxxx = dia_matrix(([diag[0],np.fliplr([diag[1]])[0],diag[1],diag[2],diag[2]],[0,1,-1,2,-2]),shape=(M,M))
        elif bc in ('LeftClampedRightFree','LeftSimplySupportedRightFree'):
            diag[0][-1] = cfs['a0']
            diag[0][-2] = 5
            diag[1][-2] = cfs['a1']
            diag2 = np.concatenate((np.zeros(M - 1),cfs['a2'] + np.zeros(1)))
            if bc == 'LeftSimplySupportedRightFree':
                diag[0][0] = 5
            tmp = np.roll(np.fliplr([diag[1]])[0],-1); tmp[-1] = -2
            Dxxxx = dia_matrix(([diag[0],tmp,diag[1],diag[2],diag[2]],[0,1,-1,2,-2]),shape=(M,M))
        elif bc in ('LeftFreeRightClamped','LeftFreeRightSimplySupported'):
            diag[0][0] = cfs['a0']
            diag[0][1] = 5
            diag[1][0] = -2
            diag2 = np.concatenate((cfs['a2'] + np.zeros(1),np.zeros(M - 1)))
            if bc == 'LeftFreeRightSimplySupported':
                diag[0][-1] = 5
            tmp = np.roll(np.fliplr([diag[1]])[0],1); tmp[1] = cfs['a1']
            Dxxxx = dia_matrix(([diag[0],tmp,diag[1],diag[2],diag[2]],[0,1,-1,2,-2]),shape=(M,M))

        return (Dxxxx,dia_matrix(([diag2,-1.0*np.roll(diag2,1),-1.0*np.roll(diag2,-1)],[0,1,-1]),shape=(M,M)))
