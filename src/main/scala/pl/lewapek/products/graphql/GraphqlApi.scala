package pl.lewapek.products.graphql

import caliban.*
import caliban.CalibanError.*
import caliban.ResponseValue.*
import caliban.Value.*
import caliban.schema.GenericSchema
import pl.lewapek.products.{AppError, Bootstrap}
import sttp.capabilities
import sttp.capabilities.zio.ZioStreams
import sttp.client3.{SttpBackend, basicRequest}
import zio.*

object GraphqlApi extends GenericSchema[Bootstrap.Requirements]:

  import Operations.*
  import Types.*

  val api: GraphQL[Bootstrap.Requirements] = graphQL[Bootstrap.Requirements, Query, Mutation, Unit](
    resolver = RootResolver(
      queryResolver = Query(
        recurrent = args => ZIO.succeed(7),
        products = Random.nextUUID.map(Report.apply)
      ),
      mutationResolver = Mutation(
        addProduct = ZIO.succeed(7)
      )
    )
  )

  def handleError[R](
      interpreter: GraphQLInterpreter[R, CalibanError]
  ): GraphQLInterpreter[R, CalibanError] =
    interpreter.mapError {
      case err @ ExecutionError(_, _, _, Some(AppError(code, message, _)), _) =>
        err.copy(extensions =
          Some(
            ObjectValue(List(("code", StringValue(code)), ("message", StringValue(message))))
          )
        )
      case err: ExecutionError =>
        err.copy(extensions = Some(ObjectValue(List(("code", StringValue("EXECUTION_ERROR"))))))
      case err: ValidationError =>
        err.copy(extensions = Some(ObjectValue(List(("code", StringValue("VALIDATION_ERROR"))))))
      case err: ParsingError =>
        err.copy(extensions = Some(ObjectValue(List(("code", StringValue("PARSING_ERROR"))))))
    }

end GraphqlApi
