// Usage : buildAndPush(image: 'user/app', tag: 'abc1234', push: true)
def call(Map config = [:]) {
  def credentialsId = config.credentialsId ?: 'dockerhub'

  withEnv(["LIB_IMAGE=${config.image}", "LIB_TAG=${config.tag}"]) {
    sh '''
      docker build \
        --label "org.opencontainers.image.revision=$GIT_COMMIT" \
        -t "$LIB_IMAGE:$LIB_TAG" .
      echo "$LIB_IMAGE:$LIB_TAG" > image.txt
    '''

    if (config.push) {
      withCredentials([usernamePassword(credentialsId: credentialsId,
                        usernameVariable: 'DH_USER', passwordVariable: 'DH_TOKEN')]) {
        sh 'echo "$DH_TOKEN" | docker login -u "$DH_USER" --password-stdin'
        sh 'docker push "$LIB_IMAGE:$LIB_TAG"'
        sh 'docker tag "$LIB_IMAGE:$LIB_TAG" "$LIB_IMAGE:latest-main"'
        sh 'docker push "$LIB_IMAGE:latest-main"'
      }
    }
  }
}
