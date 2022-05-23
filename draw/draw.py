import matplotlib
import matplotlib.pyplot as plt



rank = [5, 10, 15, 20, 25, 50, 100, 150, 200]
_lambda = [1.0, 0.5, 0.2, 0.1]
rmse1 = [1.97, 1.47, 1.63, 2.29] # rank = 5
rmse2 = [1.66, 1.37, 1.51, 1.82] # rank = 10
rmse3 = [1.67, 1.34, 1.43, 1.74] # rank = 15
rmse4 = [1.61, 1.33, 1.45, 1.68] # rank = 20
rmse5 = [1.60, 1.33, 1.40, 1.68] # rank = 25
rmse6 = [1.60, 1.33, 1.37, 1.61] # rank = 50
rmse7 = [1.60, 1.34, 1.36, 1.58] # rank = 100
rmse8 = [1.59, 1.33, 1.37, 1.58] # rank = 150
rmse9 = [1.60, 1.34, 1.36, 1.57] # rank = 200

rmse10 = [1.97, 1.66, 1.67, 1.61, 1.60, 1.60, 1.60, 1.59, 1.60] # lambda = 1.0
rmse11 = [1.47, 1.37, 1.34, 1.33, 1.33, 1.33, 1.34, 1.33, 1.34] # lambda = 0.5
rmse12 = [1.63, 1.51, 1.43, 1.45, 1.40, 1.37, 1.36, 1.37, 1.36] # lambda = 0.2
rmse13 = [2.29, 1.82, 1.74, 1.68, 1.68, 1.61, 1.58, 1.58, 1.57] # lambda = 0.1

plt.subplot(1, 2, 1)
plt.plot(_lambda, rmse1, _lambda, rmse2, _lambda, rmse3, _lambda, rmse4, _lambda, rmse5, _lambda, rmse6, _lambda, rmse7, _lambda, rmse8, _lambda, rmse9, marker='.')
plt.legend(['rank = 5', 'rank = 10', 'rank = 15', 'rank = 20', 'rank = 25', 'rank = 50', 'rank = 100', 'rank = 150', 'rank = 200'])
plt.xlabel('lambda')
plt.ylabel('RMSE')

plt.subplot(1, 2, 2)
plt.plot(rank, rmse10, rank, rmse11, rank, rmse12, rank, rmse13, marker='.')
plt.legend(['lambda = 1.0', 'lambda = 0.5', 'lambda = 0.2', 'lambda = 0.1'])
plt.xlabel('rank')
plt.ylabel('RMSE')
plt.show()