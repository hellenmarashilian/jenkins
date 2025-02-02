def checkCondition() {
    // Simulating a condition (true or false)
    def randomNumber = new Random().nextInt(2) // Generates 0 or 1
    return (randomNumber == 1) // Returns true if 1, false if 0
}
