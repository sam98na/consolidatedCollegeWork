import nn

class PerceptronModel(object):
    def __init__(self, dimensions):
        """
        Initialize a new Perceptron instance.

        A perceptron classifies data points as either belonging to a particular
        class (+1) or not (-1). `dimensions` is the dimensionality of the data.
        For example, dimensions=2 would mean that the perceptron must classify
        2D points.
        """
        self.w = nn.Parameter(1, dimensions)

    def get_weights(self):
        """
        Return a Parameter instance with the current weights of the perceptron.
        """
        return self.w

    def run(self, x):
        """
        Calculates the score assigned by the perceptron to a data point x.

        Inputs:
            x: a node with shape (1 x dimensions)
        Returns: a node containing a single number (the score)
        """
        return nn.DotProduct(x, self.w)

    def get_prediction(self, x):
        """
        Calculates the predicted class for a single data point `x`.

        Returns: 1 or -1
        """
        result = self.run(x)
        if nn.as_scalar(result) >= 0:
            return 1
        else:
            return -1

    def train(self, dataset):
        """
        Train the perceptron until convergence.
        """
        nope = True
        while nope == True:
            nope = False
            for x, y in dataset.iterate_once(1):
                pred = self.get_prediction(x)
                if pred != nn.as_scalar(y):
                    self.w.update(x, nn.as_scalar(y))
                    nope = True
                    break

class RegressionModel(object):
    """
    A neural network model for approximating a function that maps from real
    numbers to real numbers. The network should be sufficiently large to be able
    to approximate sin(x) on the interval [-2pi, 2pi] to reasonable precision.
    """
    def __init__(self):
        # Initialize your model parameters here
        self.m = nn.Parameter(1, 200)
        self.m2 = nn.Parameter(200, 200)
        self.m3 = nn.Parameter(200, 1)
        self.b = nn.Parameter(1, 200)
        self.b2 = nn.Parameter(1, 200)
        self.b3 = nn.Parameter(1, 1)
        self.learning = 0.01

    def run(self, x):
        """
        Runs the model for a batch of examples.

        Inputs:
            x: a node with shape (batch_size x 1)
        Returns:
            A node with shape (batch_size x 1) containing predicted y-values
        """
        xm = nn.Linear(x, self.m)
        bias = nn.AddBias(xm, self.b)
        relu = nn.ReLU(nn.Add(xm, bias))
        xmm = nn.Linear(relu, self.m2)
        biass = nn.AddBias(xmm, self.b2)
        reluu = nn.ReLU(nn.Add(xmm, biass))
        xm3 = nn.Linear(reluu, self.m3)
        return nn.AddBias(xm3, self.b3)

    def get_loss(self, x, y):
        """
        Computes the loss for a batch of examples.

        Inputs:
            x: a node with shape (batch_size x 1)
            y: a node with shape (batch_size x 1), containing the true y-values
                to be used for training
        Returns: a loss node
        """
        return nn.SquareLoss(self.run(x), y)

    def train(self, dataset):
        """
        Trains the model.
        """
        import time
        start = time.time()
        nope = True
        while nope ==  True:
            summed = 0
            trials = 0
            for x, y in dataset.iterate_once(100):
                loss = self.get_loss(x, y)
                summed += nn.as_scalar(loss)
                trials += 1
                if summed/trials <= 0.017:
                    nope = False
                    break
                gradm, gradb = nn.gradients(loss, [self.m, self.b])
                self.m.update(gradm, -self.learning)
                self.b.update(gradb, -self.learning)
        end = time.time()
        print("Time Elapsed (Minutes):", (end-start)/60)

class DigitClassificationModel(object):
    """
    A model for handwritten digit classification using the MNIST dataset.

    Each handwritten digit is a 28x28 pixel grayscale image, which is flattened
    into a 784-dimensional vector for the purposes of this model. Each entry in
    the vector is a floating point number between 0 and 1.

    The goal is to sort each digit into one of 10 classes (number 0 through 9).

    (See RegressionModel for more information about the APIs of different
    methods here. We recommend that you implement the RegressionModel before
    working on this part of the project.)
    """
    def __init__(self):
        # Initialize your model parameters here
        self.m = nn.Parameter(784, 250)
        self.m2 = nn.Parameter(250, 250)
        self.m3 = nn.Parameter(250, 10)
        self.b = nn.Parameter(1, 250)
        self.b2 = nn.Parameter(1, 250)
        self.b3 = nn.Parameter(1, 10)
        self.learning = 0.05

    def run(self, x):
        """
        Runs the model for a batch of examples.

        Your model should predict a node with shape (batch_size x 10),
        containing scores. Higher scores correspond to greater probability of
        the image belonging to a particular class.

        Inputs:
            x: a node with shape (batch_size x 784)
        Output:
            A node with shape (batch_size x 10) containing predicted scores
                (also called logits)
        """
        xm = nn.Linear(x, self.m)
        bias = nn.AddBias(xm, self.b)
        relu = nn.ReLU(nn.Add(xm, bias))
        xmm = nn.Linear(relu, self.m2)
        biass = nn.AddBias(xmm, self.b2)
        reluu = nn.ReLU(nn.Add(xmm, biass))
        xm3 = nn.Linear(reluu, self.m3)
        return nn.AddBias(xm3, self.b3)

    def get_loss(self, x, y):
        """
        Computes the loss for a batch of examples.

        The correct labels `y` are represented as a node with shape
        (batch_size x 10). Each row is a one-hot vector encoding the correct
        digit class (0-9).

        Inputs:
            x: a node with shape (batch_size x 784)
            y: a node with shape (batch_size x 10)
        Returns: a loss node
        """
        return nn.SoftmaxLoss(self.run(x), y)

    def train(self, dataset):
        """
        Trains the model.
        """
        import time
        start = time.time()
        nope = True
        count = 3000
        while nope:
            for x, y in dataset.iterate_once(50):
                loss = self.get_loss(x, y)
                count -= 1
                if count == 0:
                    count = 3000
                    if dataset.get_validation_accuracy() >= .975:
                        nope = False
                        break
                gradm, gradb = nn.gradients(loss, [self.m, self.b])
                self.m.update(gradm, -self.learning)
                self.b.update(gradb, -self.learning)
        end = time.time()
        print("Time Elapsed (Minutes):", (end-start)/60)

class LanguageIDModel(object):
    """
    A model for language identification at a single-word granularity.

    (See RegressionModel for more information about the APIs of different
    methods here. We recommend that you implement the RegressionModel before
    working on this part of the project.)
    """
    def __init__(self):
        # Our dataset contains words from five different languages, and the
        # combined alphabets of the five languages contain a total of 47 unique
        # characters.
        # You can refer to self.num_chars or len(self.languages) in your code
        self.num_chars = 47
        self.languages = ["English", "Spanish", "Finnish", "Dutch", "Polish"]

        # Initialize your model parameters here
        self.m = nn.Parameter(self.num_chars, 200)
        self.m2 = nn.Parameter(200, 5)
        self.b = nn.Parameter(1, 200)
        self.b2 = nn.Parameter(1, 5)
        self.w = nn.Parameter(5, 200)
        self.w2 = nn.Parameter(200, 5)
        self.learning = .9

    def run(self, xs):
        """
        Runs the model for a batch of examples.

        Although words have different lengths, our data processing guarantees
        that within a single batch, all words will be of the same length (L).

        Here `xs` will be a list of length L. Each element of `xs` will be a
        node with shape (batch_size x self.num_chars), where every row in the
        array is a one-hot vector encoding of a character. For example, if we
        have a batch of 8 three-letter words where the last word is "cat", then
        xs[1] will be a node that contains a 1 at position (7, 0). Here the
        index 7 reflects the fact that "cat" is the last word in the batch, and
        the index 0 reflects the fact that the letter "a" is the inital (0th)
        letter of our combined alphabet for this task.

        Your model should use a Recurrent Neural Network to summarize the list
        `xs` into a single node of shape (batch_size x hidden_size), for your
        choice of hidden_size. It should then calculate a node of shape
        (batch_size x 5) containing scores, where higher scores correspond to
        greater probability of the word originating from a particular language.

        Inputs:
            xs: a list with L elements (one per character), where each element
                is a node with shape (batch_size x self.num_chars)
        Returns:
            A node with shape (batch_size x 5) containing predicted scores
                (also called logits)
        """
        first = True
        h1 = nn.Parameter(1, 5)
        lenxs = len(xs)
        for i in xs:
            if first:
                lenxs -= 1
                first = False
                xm = nn.Linear(i, self.m)
                bias = nn.AddBias(xm, self.b)
                relu = nn.ReLU(nn.Add(xm, bias))
                xmm = nn.Linear(relu, self.m2)
                if lenxs == 0:
                    return nn.AddBias(xmm, self.b2)
                else:
                    h1 = nn.AddBias(xmm, self.b2)
            else:
                lenxs -= 1
                xm = nn.Add(nn.Linear(i, self.m), nn.Linear(h1, self.w))
                bias = nn.AddBias(xm, self.b)
                h1 = bias
                relu = nn.ReLU(nn.Add(xm, bias))
                xmp = nn.Linear(relu, self.m2)
                xmh = nn.Linear(h1, self.w2)
                xmm = nn.Add(xmp, xmh)
                if lenxs == 0:
                    return nn.AddBias(xmm, self.b2)
                else:
                    h1 = nn.AddBias(xmm, self.b2)


    def get_loss(self, xs, y):
        """
        Computes the loss for a batch of examples.

        The correct labels `y` are represented as a node with shape
        (batch_size x 5). Each row is a one-hot vector encoding the correct
        language.

        Inputs:
            xs: a list with L elements (one per character), where each element
                is a node with shape (batch_size x self.num_chars)
            y: a node with shape (batch_size x 5)
        Returns: a loss node
        """
        return nn.SoftmaxLoss(self.run(xs), y)

    def train(self, dataset):
        """
        Trains the model.
        """
        import time
        start = time.time()
        nope = True
        while nope:
            summed = 0
            trials = 0
            for x, y in dataset.iterate_once(50):
                loss = self.get_loss(x, y)
                trials += 1
                summed += dataset.get_validation_accuracy()
                if summed/trials >= .85:
                    nope = False
                    break
                gradm, gradb= nn.gradients(loss, [self.m, self.b])
                self.m.update(gradm, -self.learning)
                self.b.update(gradb, -self.learning)
        end = time.time()
        print("Time Elapsed (Minutes):", (end-start)/60)
