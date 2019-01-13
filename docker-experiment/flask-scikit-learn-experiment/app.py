import numpy as np
from flask import Flask
app = Flask(__name__)

@app.route("/")
def hello():
  from sklearn.linear_model import LinearRegression
  import numpy

  num_of_samples = 100
  x = numpy.random.rand(num_of_samples, 1)
  y = 3*x + 123 + numpy.random.rand(num_of_samples,1)*0.2

  reg = LinearRegression()
  reg.fit(x, y)
  s = "f(x) = %f * x + %f" % (reg.coef_[0], reg.intercept_, )
  return "Hello World! %s" % (s,)

if __name__ == '__main__':
  app.run(host='0.0.0.0', port=5000)
